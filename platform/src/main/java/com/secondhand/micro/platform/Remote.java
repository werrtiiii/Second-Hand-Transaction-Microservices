package com.secondhand.micro.platform;

import com.fasterxml.jackson.databind.*;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Remote {
  private final Tokens tokens;
  private final RestClient client;
  private final ObjectMapper json;
  private final boolean circuitBreakerEnabled;
  private final ConcurrentHashMap<String, CircuitBreaker> breakers = new ConcurrentHashMap<>();

  public Remote(Tokens tokens, ObjectMapper json,
      @Value("${app.circuit-breaker-enabled:true}") boolean circuitBreakerEnabled) {
    this.tokens = tokens;
    this.json = json;
    this.circuitBreakerEnabled = circuitBreakerEnabled;
    var factory = new JdkClientHttpRequestFactory(
        HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build());
    factory.setReadTimeout(Duration.ofSeconds(3));
    client = RestClient.builder().requestFactory(factory).build();
  }

  public JsonNode post(String base, String service, String path, Object payload, String key) {
    return call(base, service, path, payload, key, false);
  }

  public JsonNode get(String base, String service, String path) {
    return call(base, service, path, null, null, true);
  }

  /** 每个依赖服务一个熔断器；仅 5xx/网络失败触发熔断，4xx 业务错误不触发。 */
  private CircuitBreaker breaker(String service) {
    return breakers.computeIfAbsent(service, s -> CircuitBreaker.of(s, CircuitBreakerConfig.custom()
        .failureRateThreshold(50).slidingWindowSize(10).minimumNumberOfCalls(5)
        .waitDurationInOpenState(Duration.ofSeconds(10)).permittedNumberOfCallsInHalfOpenState(2)
        .recordExceptions(UpstreamFailure.class).build()));
  }

  private JsonNode call(String base, String service, String path, Object payload, String key, boolean get) {
    if (!circuitBreakerEnabled)
      return invoke(base, service, path, payload, key, get);
    try {
      return breaker(service).executeSupplier(() -> invoke(base, service, path, payload, key, get));
    } catch (CallNotPermittedException e) {
      throw new Failure(503, "CIRCUIT_OPEN", "依赖服务暂不可用（熔断保护中，请稍后重试）");
    }
  }

  private JsonNode invoke(String base, String service, String path, Object payload, String key, boolean get) {
    try {
      var request = client
          .method(get ? org.springframework.http.HttpMethod.GET : org.springframework.http.HttpMethod.POST)
          .uri(base + path).header("Authorization", "Bearer " + tokens.serviceToken(service));
      if (key != null)
        request.header("Idempotency-Key", key);
      if (!get)
        request.contentType(org.springframework.http.MediaType.APPLICATION_JSON).body(payload);
      JsonNode result = request.retrieve().body(JsonNode.class);
      if (result == null || !result.path("success").asBoolean())
        throw new UpstreamFailure(502, "BAD_GATEWAY", "上游响应无效");
      return result.path("data");
    } catch (RestClientResponseException e) {
      String code = "UPSTREAM_ERROR", message = "上游拒绝请求";
      try {
        var error = json.readTree(e.getResponseBodyAsString()).path("error");
        code = error.path("code").asText(code);
        message = error.path("message").asText(message);
      } catch (Exception ignored) {
      }
      if (e.getStatusCode().value() >= 500)
        throw new UpstreamFailure(e.getStatusCode().value(), code, message);
      throw new Failure(e.getStatusCode().value(), code, message);
    } catch (ResourceAccessException e) {
      throw new UpstreamFailure(503, "DEPENDENCY_UNAVAILABLE", "依赖服务暂不可用");
    }
  }

  /** 依赖服务不可用（5xx/网络/超时）的标记异常，仅用于计数触发熔断，不改对外 HTTP 状态语义。 */
  public static class UpstreamFailure extends Failure {
    public UpstreamFailure(int status, String code, String message) {
      super(status, code, message);
    }
  }
}
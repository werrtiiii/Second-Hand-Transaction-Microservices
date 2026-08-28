package com.secondhand.micro.platform;
import com.fasterxml.jackson.databind.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import java.net.http.HttpClient;
import java.time.Duration;
@Component
public class Remote {
 private final Tokens tokens; private final RestClient client; private final ObjectMapper json;
 public Remote(Tokens tokens,ObjectMapper json){
  this.tokens=tokens;this.json=json;
  var factory=new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build());
  factory.setReadTimeout(Duration.ofSeconds(3));
  client=RestClient.builder().requestFactory(factory).build();
 }
 public JsonNode post(String base,String service,String path,Object payload,String key){return call(base,service,path,payload,key,false);}
 public JsonNode get(String base,String service,String path){return call(base,service,path,null,null,true);}
 private JsonNode call(String base,String service,String path,Object payload,String key,boolean get){
  try{
   var request=client.method(get?org.springframework.http.HttpMethod.GET:org.springframework.http.HttpMethod.POST).uri(base+path).header("Authorization","Bearer "+tokens.serviceToken(service));
   if(key!=null)request.header("Idempotency-Key",key);
   if(!get)request.contentType(org.springframework.http.MediaType.APPLICATION_JSON).body(payload);
   JsonNode result=request.retrieve().body(JsonNode.class);
   if(result==null||!result.path("success").asBoolean())throw new Failure(502,"BAD_GATEWAY","上游响应无效");
   return result.path("data");
  }catch(RestClientResponseException e){throw new Failure(e.getStatusCode().value(),"UPSTREAM_ERROR","上游拒绝请求");}
   catch(ResourceAccessException e){throw new Failure(503,"DEPENDENCY_UNAVAILABLE","依赖服务暂不可用");}
 }
}

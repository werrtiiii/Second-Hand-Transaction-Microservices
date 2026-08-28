package com.secondhand.micro.platform;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Map;
@Component
public class IdentityClient {
 private final Remote remote; private final String userUrl;
 public IdentityClient(Remote remote,@Value("${app.user-url:http://user-service:8080}") String url){this.remote=remote;userUrl=url;}
 public long requireUser(String header){
  String token=Tokens.bearer(header);
  var result=remote.post(userUrl,"user-service","/internal/v1/auth/introspect",Map.of("token",token),null);
  if(!result.path("active").asBoolean())throw new Failure(401,"UNAUTHORIZED","登录已失效");
  return result.path("userId").asLong();
 }
}

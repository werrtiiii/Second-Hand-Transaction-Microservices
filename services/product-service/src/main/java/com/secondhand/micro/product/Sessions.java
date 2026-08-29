package com.secondhand.micro.product;
import com.secondhand.micro.security.SessionResolver;
import com.secondhand.auth.security.AuthPrincipal;
import com.secondhand.micro.platform.*;
@org.springframework.stereotype.Component
public class Sessions implements SessionResolver {
 private final Remote remote; private final String userUrl; public Sessions(Remote r,@org.springframework.beans.factory.annotation.Value("${app.user-url}") String u){remote=r;userUrl=u;}
 public AuthPrincipal resolve(String header){if(header==null||!header.startsWith("Bearer "))throw new Failure(401,"UNAUTHORIZED","请登录");
 var data=remote.post(userUrl,"user-service","/internal/v1/auth/introspect",java.util.Map.of("token",header.substring(7)),null);
 if(!data.path("active").asBoolean())throw new Failure(401,"UNAUTHORIZED","登录已失效");
 return new AuthPrincipal(data.path("userId").asLong(),data.path("roles").get(0).asText());}
}

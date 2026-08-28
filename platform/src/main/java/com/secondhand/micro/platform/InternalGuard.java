package com.secondhand.micro.platform;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
@Component
public class InternalGuard extends OncePerRequestFilter {
 public static final String CALLER="verified-service-caller";
 private final Tokens tokens; private final ObjectMapper json;
 public InternalGuard(Tokens tokens,ObjectMapper json){this.tokens=tokens;this.json=json;}
 protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
  if(req.getRequestURI().startsWith("/internal/")){
   try{req.setAttribute(CALLER,tokens.verifyService(req.getHeader("Authorization")));}
   catch(Failure e){res.setStatus(e.status);res.setContentType("application/json;charset=UTF-8");json.writeValue(res.getOutputStream(),Api.fail(e.code,e.getMessage()));return;}
  }
  chain.doFilter(req,res);
 }
 public static void require(HttpServletRequest req,String... allowed){
  Object caller=req.getAttribute(CALLER);
  for(String service:allowed)if(service.equals(caller))return;
  throw Failure.forbidden();
 }
}

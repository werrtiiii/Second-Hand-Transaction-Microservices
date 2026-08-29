package com.secondhand.common.ratelimit;
import jakarta.servlet.http.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.method.HandlerMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.concurrent.ConcurrentHashMap;
/** 实例内固定窗口限流；不信任外部自填的 X-Forwarded-For。 */
@Configuration @ConditionalOnProperty(name="app.rate-limit-enabled",havingValue="true",matchIfMissing=true)
public class RateLimitConfig implements WebMvcConfigurer {
 private record Window(long until,int count){}
 private final ConcurrentHashMap<String,Window> windows=new ConcurrentHashMap<>();
 public void addInterceptors(InterceptorRegistry registry){registry.addInterceptor(new HandlerInterceptor(){
  public boolean preHandle(HttpServletRequest req,HttpServletResponse res,Object handler)throws Exception{
   if(!(handler instanceof HandlerMethod method))return true;
   RateLimit limit=method.getMethodAnnotation(RateLimit.class);if(limit==null)return true;
   long now=System.currentTimeMillis();
   if(windows.size()>10000)windows.entrySet().removeIf(e->e.getValue().until()<=now);
   var auth=SecurityContextHolder.getContext().getAuthentication();
   String client=auth!=null&&auth.isAuthenticated()&&!"anonymousUser".equals(auth.getPrincipal())?auth.getName():req.getRemoteAddr();
   String key=client+":"+method.getMethod().toGenericString();
   Window w=windows.compute(key,(k,old)->old==null||old.until()<=now?new Window(now+limit.windowSeconds()*1000L,1):new Window(old.until(),old.count()+1));
   if(w.count()<=limit.maxRequests())return true;
   res.setStatus(429);res.setHeader("Retry-After",Long.toString(Math.max(1,(w.until()-now+999)/1000)));
   res.setContentType("application/json;charset=UTF-8");res.getWriter().write("{\"success\":false,\"error\":{\"code\":\"RATE_LIMITED\",\"message\":\"请求过于频繁，请稍后再试\"}}");return false;
  }
 });}
}

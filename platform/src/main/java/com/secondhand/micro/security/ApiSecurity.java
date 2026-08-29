package com.secondhand.micro.security;
import com.secondhand.auth.security.AuthPrincipal;
import com.secondhand.micro.platform.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
@Configuration @EnableMethodSecurity
public class ApiSecurity {
 @Bean SecurityFilterChain apiSecurity(HttpSecurity http,SessionResolver sessions)throws Exception{
  var filter=new OncePerRequestFilter(){
   protected void doFilterInternal(HttpServletRequest r,HttpServletResponse s,FilterChain chain)throws ServletException,IOException{
    if(r.getRequestURI().startsWith("/api/")&&r.getHeader("Authorization")!=null){
     try{AuthPrincipal p=sessions.resolve(r.getHeader("Authorization"));SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(p,null,p.getAuthorities()));}
     catch(Failure e){s.setStatus(e.status);s.setContentType("application/json;charset=UTF-8");s.getWriter().write("{\"success\":false,\"error\":{\"code\":\""+e.code+"\",\"message\":\"身份校验失败\"}}");return;}
    }
    chain.doFilter(r,s);
   }
  };
  http.csrf(c->c.disable()).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(a->a
    .requestMatchers("/api/auth/register","/api/auth/login").permitAll()
    .requestMatchers("/api/admin/**","/api/orders/process-settlements","/api/after-sales/process-timeouts").hasRole("ADMIN")
    .requestMatchers(HttpMethod.GET,"/api/products","/api/products/*","/api/products/*/comments","/api/categories","/api/regions","/api/users/*/public","/api/users/*/products","/api/users/*/sold","/api/users/*/rating","/api/shipments/*/track").permitAll()
    .requestMatchers("/api/**").authenticated().anyRequest().permitAll())
   .exceptionHandling(e->e.authenticationEntryPoint((r,s,x)->{s.setStatus(401);s.setContentType("application/json");s.getWriter().write("{\"success\":false,\"error\":{\"code\":\"UNAUTHORIZED\"}}");})
    .accessDeniedHandler((r,s,x)->{s.setStatus(403);s.setContentType("application/json");s.getWriter().write("{\"success\":false,\"error\":{\"code\":\"FORBIDDEN\"}}");}))
   .addFilterBefore(filter,UsernamePasswordAuthenticationFilter.class);
  return http.build();
 }
}

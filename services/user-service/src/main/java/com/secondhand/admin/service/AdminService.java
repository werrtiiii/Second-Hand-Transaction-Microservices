package com.secondhand.admin.service;
import com.secondhand.micro.platform.*;
import com.secondhand.admin.OnlineUserTracker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.*;
@Service @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name="app.service-name",havingValue="user-service")
public class AdminService {
 private final Remote remote;private final JdbcTemplate db;private final String trade,product;private final OnlineUserTracker online;
 public AdminService(Remote r,JdbcTemplate d,OnlineUserTracker o,@Value("${app.trade-url}") String t,@Value("${app.product-url}") String p){remote=r;db=d;trade=t;product=p;online=o;}
 public Map<String,Object> getDashboard(){
  var t=remote.get(trade,"trade-service","/internal/v1/trade/stats");var p=remote.get(product,"product-service","/internal/v1/products/stats");
  var result=new LinkedHashMap<String,Object>();result.put("totalUsers",db.queryForObject("SELECT COUNT(*) FROM users",Long.class));result.put("todayNewUsers",db.queryForObject("SELECT COUNT(*) FROM users WHERE created_at>=CURRENT_DATE",Long.class));result.put("activeUsers",online.countActive(10));
  for(String key:List.of("totalProducts","onSaleProducts"))result.put(key,p.path(key).asLong());
  result.put("totalOrders",t.path("totalOrders").asLong());result.put("todayNewOrders",t.path("todayNewOrders").asLong());result.put("orderStatusDistribution",t.path("orderStatusDistribution"));return result;
 }
}

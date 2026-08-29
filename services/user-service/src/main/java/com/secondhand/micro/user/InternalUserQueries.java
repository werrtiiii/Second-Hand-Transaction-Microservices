package com.secondhand.micro.user;
import com.secondhand.micro.platform.*;
import com.secondhand.admin.OnlineUserTracker;
import com.fasterxml.jackson.databind.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController public class InternalUserQueries {
 private final JdbcTemplate db;private final ObjectMapper json;private final OnlineUserTracker online;
 public InternalUserQueries(JdbcTemplate d,ObjectMapper j,OnlineUserTracker o){db=d;json=j;online=o;}
 public record Event(@NotBlank @Size(max=64) String eventId,@Positive long recipientId,@Pattern(regexp="system|comment") String kind,@NotNull JsonNode payload){}
 @PostMapping("/internal/v1/notifications") public Api<?> consume(HttpServletRequest req,@Valid @RequestBody Event e)throws Exception{
  InternalGuard.require(req,"product-service","trade-service");String source=(String)req.getAttribute(InternalGuard.CALLER);
  if(source==null)source=(String)req.getAttribute("service-caller");
  // 来源从经过验签的调用方获取，不能信任请求体里的来源字段。
  if(source==null)throw new IllegalStateException("Missing verified caller");
  if("comment".equals(e.kind())&&e.payload().isObject()){
   var users=db.queryForList("SELECT nickname,avatar_url FROM users WHERE id=?",e.payload().path("commenterId").asLong());
   if(!users.isEmpty()){((com.fasterxml.jackson.databind.node.ObjectNode)e.payload()).put("commenterName",(String)users.get(0).get("nickname"));((com.fasterxml.jackson.databind.node.ObjectNode)e.payload()).put("commenterAvatar",(String)users.get(0).get("avatar_url"));}
  }
  db.update("INSERT IGNORE INTO notifications(id,source_service,recipient_id,kind,payload,created_at) VALUES(?,?,?,?,?,NOW())",source+":"+e.eventId(),source,e.recipientId(),e.kind(),json.writeValueAsString(e.payload()));return Api.ok(null);
 }
 @GetMapping("/internal/v1/users/stats") public Api<?> stats(HttpServletRequest r){InternalGuard.require(r,"trade-service");return Api.ok(Map.of("totalUsers",db.queryForObject("SELECT COUNT(*) FROM users",Long.class),"todayNewUsers",db.queryForObject("SELECT COUNT(*) FROM users WHERE created_at>=CURRENT_DATE",Long.class),"activeUsers",online.countActive(10)));}
 public record Address(@Positive long userId,@Positive long addressId){}
 @PostMapping("/internal/v1/addresses/resolve") public Api<?> address(HttpServletRequest r,@Valid @RequestBody Address a){
  InternalGuard.require(r,"trade-service");var rows=db.queryForList("SELECT * FROM user_addresses WHERE id=? AND user_id=?",a.addressId(),a.userId());if(rows.isEmpty())throw Failure.missing();return Api.ok(rows.get(0));
 }
}

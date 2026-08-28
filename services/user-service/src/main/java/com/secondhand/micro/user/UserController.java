package com.secondhand.micro.user;
import com.secondhand.micro.platform.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
public class UserController {
 private final UserStore users;
 public UserController(UserStore users){this.users=users;}
 public record Credentials(@NotBlank @Pattern(regexp="PHONE|EMAIL") String identityType,@NotBlank @Size(max=128) String identifier,@NotBlank @Size(min=6,max=64) String password){}
 public record Introspection(@NotBlank String token){}
 public record Batch(@NotNull @Size(max=100) List<@NotNull @Positive Long> userIds){}
 @PostMapping("/api/auth/register") ResponseEntity<?> register(@Valid @RequestBody Credentials r){return ResponseEntity.status(201).body(Api.ok(users.register(r.identityType(),r.identifier(),r.password())));}
 @PostMapping("/api/auth/login") Api<?> login(@Valid @RequestBody Credentials r){return Api.ok(users.login(r.identityType(),r.identifier(),r.password()));}
 @GetMapping("/api/auth/me") Api<?> me(@RequestHeader(value="Authorization",required=false) String auth){return Api.ok(users.me(auth));}
 @PostMapping("/internal/v1/auth/introspect") Api<?> inspect(HttpServletRequest req,@Valid @RequestBody Introspection r){InternalGuard.require(req,"product-service","trade-service");return Api.ok(users.introspect(r.token()));}
 @PostMapping("/internal/v1/users/batch") Api<?> batch(HttpServletRequest req,@Valid @RequestBody Batch r){
  InternalGuard.require(req,"product-service","trade-service");var items=users.summaries(r.userIds());
  var found=items.stream().map(x->((Number)x.get("userId")).longValue()).toList();
  return Api.ok(Map.of("items",items,"missingIds",r.userIds().stream().filter(id->!found.contains(id)).toList()));
 }
}

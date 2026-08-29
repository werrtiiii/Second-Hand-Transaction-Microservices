package com.secondhand.auth.controller;
import com.secondhand.micro.user.UserStore;
import com.secondhand.admin.OnlineUserTracker;
import com.secondhand.auth.security.AuthPrincipal;
import com.secondhand.auth.dto.ChangePasswordRequest;
import com.secondhand.micro.platform.Api;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/auth")
public class AuthController {
 private final UserStore users; private final OnlineUserTracker online;
 public AuthController(UserStore users,OnlineUserTracker online){this.users=users;this.online=online;}
 @PostMapping("/heartbeat") public Api<?> heartbeat(@AuthenticationPrincipal AuthPrincipal p){online.heartbeat(p.userId());return Api.ok(null);}
 @PostMapping("/password/change") public Api<?> password(@AuthenticationPrincipal AuthPrincipal p,@Valid @RequestBody ChangePasswordRequest r){users.changePassword(p.userId(),r.oldPassword(),r.newPassword());return Api.ok(null);}
}

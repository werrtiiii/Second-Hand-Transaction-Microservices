package com.secondhand.micro.user;
import com.secondhand.micro.security.SessionResolver;
import com.secondhand.auth.security.AuthPrincipal;
import com.secondhand.micro.platform.*;
@org.springframework.stereotype.Component
public class Sessions implements SessionResolver {
 private final UserStore users; public Sessions(UserStore u){users=u;}
 public AuthPrincipal resolve(String header){var u=users.me(header);return new AuthPrincipal(u.userId(),u.role());}
}

package com.secondhand.micro.security;
import com.secondhand.auth.security.AuthPrincipal;
public interface SessionResolver { AuthPrincipal resolve(String authorization); }

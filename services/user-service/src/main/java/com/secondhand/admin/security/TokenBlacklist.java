package com.secondhand.admin.security;
@org.springframework.stereotype.Component public class TokenBlacklist {
 private final org.springframework.jdbc.core.JdbcTemplate db;
 public TokenBlacklist(org.springframework.jdbc.core.JdbcTemplate db){this.db=db;}
 public void add(long id){db.update("UPDATE user_security_state SET token_version=token_version+1 WHERE user_id=?",id);}
}

package com.secondhand.micro.user;
import com.secondhand.micro.platform.*;
import io.jsonwebtoken.Claims;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import java.sql.Statement;
import java.util.*;
@Service
public class UserStore {
 private final JdbcTemplate db; private final TransactionTemplate tx; private final Tokens tokens;
 private final BCryptPasswordEncoder passwords=new BCryptPasswordEncoder();
 public UserStore(JdbcTemplate db,PlatformTransactionManager manager,Tokens tokens){this.db=db;this.tx=new TransactionTemplate(manager);this.tokens=tokens;}
 public record AuthResponse(String accessToken,long userId,String nickname,String role,String avatarUrl){}
 private String normalize(String type,String identifier){
  String value=identifier.trim();
  if("EMAIL".equals(type)){value=value.toLowerCase(Locale.ROOT);if(!value.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))throw new Failure(400,"BAD_REQUEST","邮箱格式错误");}
  else if(!"PHONE".equals(type)||!value.matches("1[3-9][0-9]{9}"))throw new Failure(400,"BAD_REQUEST","手机号格式错误");
  return value;
 }
 public AuthResponse register(String type,String identifier,String password){
  String value=normalize(type,identifier);
  if(db.queryForObject("SELECT COUNT(*) FROM user_identities WHERE identity_type=? AND identifier=?",Long.class,type,value)>0)throw new Failure(409,"IDENTITY_EXISTS","登录标识已注册");
  return tx.execute(status->{
   var key=new GeneratedKeyHolder();
   db.update(c->{var p=c.prepareStatement("INSERT INTO users(nickname,password_hash,role,status,created_at,updated_at) VALUES(?,?,'USER','ACTIVE',NOW(),NOW())",Statement.RETURN_GENERATED_KEYS);p.setString(1,"新用户");p.setString(2,passwords.encode(password));return p;},key);
   long id=key.getKey().longValue();
   db.update("INSERT INTO user_identities(user_id,identity_type,identifier,verified,created_at,updated_at) VALUES(?,?,?,0,NOW(),NOW())",id,type,value);
   db.update("INSERT INTO user_security_state(user_id,token_version) VALUES(?,0)",id);
   db.update("UPDATE users SET nickname=?,phone=?,email=? WHERE id=?","用户"+id,"PHONE".equals(type)?value:null,"EMAIL".equals(type)?value:null,id);
   return response(id,true);
  });
 }
 public void changePassword(long id,String oldPassword,String newPassword){
  tx.executeWithoutResult(s->{String hash=db.queryForObject("SELECT password_hash FROM users WHERE id=? FOR UPDATE",String.class,id);
   if(!passwords.matches(oldPassword,hash))throw new Failure(401,"INVALID_CREDENTIALS","旧密码不正确");
   db.update("UPDATE users SET password_hash=?,updated_at=NOW() WHERE id=?",passwords.encode(newPassword),id);
   db.update("UPDATE user_security_state SET token_version=token_version+1 WHERE user_id=?",id);
  });
 }
 public AuthResponse login(String type,String identifier,String password){
  var rows=db.queryForList("SELECT u.* FROM users u JOIN user_identities i ON i.user_id=u.id WHERE i.identity_type=? AND i.identifier=?",type,normalize(type,identifier));
  if(rows.isEmpty()||!passwords.matches(password,(String)rows.get(0).get("password_hash")))throw new Failure(401,"INVALID_CREDENTIALS","账号或密码错误");
  return response(((Number)rows.get(0).get("id")).longValue(),true);
 }
 public AuthResponse me(String header){return response(active(header),false);}
 public long active(String header){
  Claims c=tokens.verifyUser(header);long id;
  try{id=Long.parseLong(c.getSubject());}catch(Exception e){throw new Failure(401,"UNAUTHORIZED","凭证无效");}
  var rows=db.queryForList("SELECT u.status,s.token_version FROM users u JOIN user_security_state s ON s.user_id=u.id WHERE u.id=?",id);
  if(rows.isEmpty()||!"ACTIVE".equals(rows.get(0).get("status"))||c.get("version",Number.class)==null||c.get("version",Number.class).longValue()!=((Number)rows.get(0).get("token_version")).longValue())
   throw new Failure(401,"UNAUTHORIZED","账号或登录状态已失效");
  return id;
 }
 public Map<String,Object> introspect(String token){
  try {long id=active("Bearer "+token);var u=response(id,false);return Map.of("active",true,"userId",id,"roles",List.of(u.role()));}
  catch(Failure e){if(e.status==401||e.status==403)return Map.of("active",false);throw e;}
 }
 private AuthResponse response(long id,boolean issue){
  var rows=db.queryForList("SELECT u.*,s.token_version FROM users u JOIN user_security_state s ON s.user_id=u.id WHERE u.id=?",id);
  if(rows.isEmpty())throw Failure.missing();var u=rows.get(0);
  if(!"ACTIVE".equals(u.get("status")))throw Failure.forbidden();
  String role=(String)u.get("role");
  return new AuthResponse(issue?tokens.userToken(id,role,((Number)u.get("token_version")).longValue()):null,id,(String)u.get("nickname"),role,(String)u.get("avatar_url"));
 }
 public List<Map<String,Object>> summaries(List<Long> ids){
  if(ids.isEmpty())return List.of();
  return db.query("SELECT id,nickname,avatar_url FROM users WHERE id IN ("+String.join(",",Collections.nCopies(ids.size(),"?"))+")",
   (rs,n)->{Map<String,Object> r=new LinkedHashMap<>();r.put("userId",rs.getLong("id"));r.put("nickname",rs.getString("nickname"));r.put("avatarUrl",rs.getString("avatar_url"));return r;},ids.toArray());
 }
}

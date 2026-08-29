package com.secondhand.micro.user;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
/** 仅显式配置时初始化管理员，不将第一个注册用户自动提权。 */
@Component @ConditionalOnProperty(name="app.bootstrap-admin.enabled",havingValue="true")
public class AdminBootstrap implements CommandLineRunner {
 private final UserStore users;private final JdbcTemplate db;private final String identifier,password;
 public AdminBootstrap(UserStore u,JdbcTemplate d,@Value("${app.bootstrap-admin.identifier}") String i,@Value("${app.bootstrap-admin.password}") String p){users=u;db=d;identifier=i;password=p;}
 public void run(String...args){
  if(password.length()<12)throw new IllegalStateException("管理员初始化密码至少12位");
  var existing=db.queryForList("SELECT u.role FROM users u JOIN user_identities i ON i.user_id=u.id WHERE i.identity_type='PHONE' AND i.identifier=?",identifier);
  if(!existing.isEmpty()){if(!"ADMIN".equals(existing.get(0).get("role")))throw new IllegalStateException("不能把已有普通账号自动提权");return;}
  var admin=users.register("PHONE",identifier,password);db.update("UPDATE users SET role='ADMIN' WHERE id=?",admin.userId());
 }
}

package com.secondhand.micro.user;
import com.secondhand.micro.testing.*;
import org.junit.jupiter.api.*;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
class UserApiIT {
 static TestEnvironment env;static String url;
 @BeforeAll static void start(){env=new TestEnvironment();url=TestEnvironment.http(env.start("user",UserApplication.class,Map.of()));}
 @AfterAll static void stop(){env.close();}
 static Map<String,Object> credentials(String email){return Map.of("identityType","EMAIL","identifier",email,"password","password123");}
 @Test void registerLoginAndMe(){
  // 通过真实 HTTP 和 MySQL 验证身份签发及当前用户查询。
  var registered=Http.call(url,"POST","/api/auth/register",credentials("one@example.com"),null,null);assertEquals(201,registered.status());
  var login=Http.call(url,"POST","/api/auth/login",credentials("one@example.com"),null,null);assertEquals(200,login.status());
  var me=Http.call(url,"GET","/api/auth/me",null,login.data().path("accessToken").asText(),null);assertEquals(registered.data().path("userId"),me.data().path("userId"));
 }
 @Test void duplicateRegistrationRollsBackUser(){
  Http.call(url,"POST","/api/auth/register",credentials("duplicate@example.com"),null,null);
  long before=env.db("user").queryForObject("SELECT COUNT(*) FROM users",Long.class);
  assertEquals(409,Http.call(url,"POST","/api/auth/register",credentials("duplicate@example.com"),null,null).status());
  assertEquals(before,env.db("user").queryForObject("SELECT COUNT(*) FROM users",Long.class));
 }
 @Test void wrongPasswordAndMissingTokenAreRejected(){
  Http.call(url,"POST","/api/auth/register",credentials("wrong@example.com"),null,null);
  assertEquals(401,Http.call(url,"POST","/api/auth/login",Map.of("identityType","EMAIL","identifier","wrong@example.com","password","wrongpass"),null,null).status());
  assertEquals(401,Http.call(url,"GET","/api/auth/me",null,null,null).status());
 }
 @Test void revokedVersionInvalidatesPreviouslyIssuedToken(){
  var registered=Http.call(url,"POST","/api/auth/register",credentials("revoked@example.com"),null,null);
  env.db("user").update("UPDATE user_security_state SET token_version=token_version+1 WHERE user_id=?",registered.data().path("userId").asLong());
  var introspect=Http.call(url,"POST","/internal/v1/auth/introspect",Map.of("token",registered.data().path("accessToken").asText()),env.tokens("trade").serviceToken("user-service"),null);
  assertEquals(200,introspect.status());assertFalse(introspect.data().path("active").asBoolean());
 }
 @Test void invalidInputAndUnauthenticatedInternalRequestAreRejected(){
  assertEquals(400,Http.call(url,"POST","/api/auth/register",credentials("not-an-email"),null,null).status());
  assertEquals(401,Http.call(url,"POST","/internal/v1/users/batch",Map.of("userIds",java.util.List.of(1)),null,null).status());
 }
}

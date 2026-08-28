package com.secondhand.micro.platform;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.*;
import java.security.spec.*;
import java.time.Instant;
import java.util.*;
@Component
public class Tokens {
 private final String service; private final PrivateKey privateKey; private final Map<String,PublicKey> publicKeys;
 public Tokens(@Value("${app.service-name}") String service,@Value("${app.private-key}") String privateKey,
  @Value("${app.user-public-key}") String user,@Value("${app.product-public-key}") String product,@Value("${app.trade-public-key}") String trade) throws Exception {
  this.service=service; var factory=KeyFactory.getInstance("RSA");
  this.privateKey=factory.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey)));
  publicKeys=Map.of("user-service",decode(factory,user),"product-service",decode(factory,product),"trade-service",decode(factory,trade));
 }
 private PublicKey decode(KeyFactory f,String value)throws Exception{return f.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(value)));}
 public String serviceToken(String audience){return issue(audience,"service",service,Map.of(),60);}
 public String userToken(long id,String role,long version){return issue("secondhand-web","user",Long.toString(id),Map.of("role",role,"version",version),3600);}
 private String issue(String audience,String kind,String subject,Map<String,Object> extra,int lifetime){
  Instant now=Instant.now();
  return Jwts.builder().issuer(service).subject(subject).audience().add(audience).and().claims(extra).claim("kind",kind)
   .id(UUID.randomUUID().toString()).issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(lifetime))).signWith(privateKey,Jwts.SIG.RS256).compact();
 }
 private Claims verify(String token,String issuer,String audience,String kind){
  try {
   Claims c=Jwts.parser().verifyWith(publicKeys.get(issuer)).requireIssuer(issuer).requireAudience(audience).require("kind",kind).build().parseSignedClaims(token).getPayload();
   if(c.getExpiration()==null || c.getIssuedAt()==null)throw new IllegalArgumentException();
   return c;
  }catch(Exception e){throw new Failure(401,"UNAUTHORIZED","凭证无效或已过期");}
 }
 // 逐个受信公钥验签，不相信未验证的 issuer，更不把用户令牌当服务令牌。
 public String verifyService(String bearer){
  String token=bearer(bearer);
  for(String issuer:publicKeys.keySet()){try{verify(token,issuer,service,"service");return issuer;}catch(Failure ignored){}}
  throw new Failure(401,"UNAUTHORIZED","服务凭证无效");
 }
 public Claims verifyUser(String bearer){return verify(bearer(bearer),"user-service","secondhand-web","user");}
 public static String bearer(String header){if(header==null||!header.startsWith("Bearer "))throw new Failure(401,"UNAUTHORIZED","缺少凭证");return header.substring(7);}
}

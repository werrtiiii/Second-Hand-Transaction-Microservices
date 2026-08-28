package com.secondhand.micro.platform;
import java.nio.file.*;
import java.security.*;
import java.util.*;
/** 只生成本地开发凭证；不输出私钥，也不覆盖已有 .env。 */
public final class GenerateDevEnvironment {
 public static void main(String[] args)throws Exception{
  Path target=Path.of(".env");
  if(Files.exists(target))throw new IllegalStateException(".env 已存在，拒绝覆盖");
  var lines=new ArrayList<String>();var random=new SecureRandom();
  for(String key:List.of("MYSQL_ROOT_PASSWORD","USER_DB_PASSWORD","PRODUCT_DB_PASSWORD","TRADE_DB_PASSWORD")){byte[] bytes=new byte[24];random.nextBytes(bytes);lines.add(key+"="+HexFormat.of().formatHex(bytes));}
  for(String service:List.of("USER","PRODUCT","TRADE")){
   var generator=KeyPairGenerator.getInstance("RSA");generator.initialize(2048);var pair=generator.generateKeyPair();
   lines.add(service+"_PRIVATE_KEY="+Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()));
   lines.add(service+"_PUBLIC_KEY="+Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));
  }
  Files.write(target,lines,StandardOpenOption.CREATE_NEW);
  System.out.println("已生成本地 .env；请限制文件访问权限，不要提交或共享此文件。");
 }
}

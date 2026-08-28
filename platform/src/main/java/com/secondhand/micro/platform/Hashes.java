package com.secondhand.micro.platform;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
public final class Hashes {
 private Hashes(){}
 public static String sha256(String text){
  try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));}
  catch(Exception e){throw new IllegalStateException(e);}
 }
 public static void key(String key){if(key==null||!key.matches("[A-Za-z0-9:_-]{1,80}"))throw new Failure(400,"BAD_REQUEST","幂等键须为 1-80 位字母数字或 :_-");}
}

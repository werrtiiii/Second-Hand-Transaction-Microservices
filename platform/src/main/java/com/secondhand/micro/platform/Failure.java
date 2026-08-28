package com.secondhand.micro.platform;
public class Failure extends RuntimeException {
 public final int status; public final String code;
 public Failure(int status,String code,String message){super(message);this.status=status;this.code=code;}
 public static Failure conflict(String message){return new Failure(409,"CONFLICT",message);}
 public static Failure forbidden(){return new Failure(403,"FORBIDDEN","无权访问该资源");}
 public static Failure missing(){return new Failure(404,"NOT_FOUND","资源不存在");}
}

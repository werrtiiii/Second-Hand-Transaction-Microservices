package com.secondhand.micro.platform;
public record Api<T>(boolean success,T data,Error error) {
 public record Error(String code,String message) {}
 public static <T> Api<T> ok(T data){return new Api<>(true,data,null);}
 public static Api<Void> fail(String code,String message){return new Api<>(false,null,new Error(code,message));}
}

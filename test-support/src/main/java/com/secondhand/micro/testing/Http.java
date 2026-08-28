package com.secondhand.micro.testing;
import com.fasterxml.jackson.databind.*;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
public final class Http {
 private static final ObjectMapper JSON=new ObjectMapper();
 private static final HttpClient CLIENT=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
 public record Result(int status,JsonNode body){public JsonNode data(){return body.path("data");}}
 public static Result call(String base,String method,String path,Object body,String bearer,String key){
  try{
   var request=HttpRequest.newBuilder(URI.create(base+path)).timeout(Duration.ofSeconds(15));
   if(bearer!=null)request.header("Authorization","Bearer "+bearer);
   if(key!=null)request.header("Idempotency-Key",key);
   request.header("Content-Type","application/json");
   request.method(method,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(body)));
   var response=CLIENT.send(request.build(),HttpResponse.BodyHandlers.ofString());
   return new Result(response.statusCode(),JSON.readTree(response.body()));
  }catch(Exception e){throw new IllegalStateException(e);}
 }
}

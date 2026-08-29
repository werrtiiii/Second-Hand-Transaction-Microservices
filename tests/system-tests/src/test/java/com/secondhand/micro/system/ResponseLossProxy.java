package com.secondhand.micro.system;
import com.sun.net.httpserver.HttpServer;
import java.net.*;
import java.net.http.*;
import java.util.concurrent.*;
class ResponseLossProxy implements AutoCloseable {
 final HttpServer server;final ExecutorService executor=Executors.newCachedThreadPool();volatile boolean dropReservationReplies;volatile boolean dropNotificationReplies;
 ResponseLossProxy(String target)throws Exception{
  server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);server.setExecutor(executor);
  server.createContext("/",exchange->{
   try{
    var builder=HttpRequest.newBuilder(URI.create(target+exchange.getRequestURI())).method(exchange.getRequestMethod(),HttpRequest.BodyPublishers.ofByteArray(exchange.getRequestBody().readAllBytes()));
    for(String header:new String[]{"Authorization","Idempotency-Key","Content-Type"}){String value=exchange.getRequestHeaders().getFirst(header);if(value!=null)builder.header(header,value);}
    var response=HttpClient.newHttpClient().send(builder.build(),HttpResponse.BodyHandlers.ofByteArray());
    // 请求已由真实商品服务处理，仅丢弃响应，模拟结果未知。
    if(dropReservationReplies&&exchange.getRequestURI().getPath().equals("/internal/v1/inventory/reservations")&&exchange.getRequestMethod().equals("POST")){exchange.close();return;}
    if(dropNotificationReplies&&exchange.getRequestURI().getPath().equals("/internal/v1/notifications")){exchange.close();return;}
    exchange.getResponseHeaders().set("Content-Type","application/json");
    exchange.sendResponseHeaders(response.statusCode(),response.body().length);exchange.getResponseBody().write(response.body());
   }catch(Exception e){try{exchange.sendResponseHeaders(502,-1);}catch(Exception ignored){}}
   finally{exchange.close();}
  });server.start();
 }
 String url(){return "http://127.0.0.1:"+server.getAddress().getPort();}
 public void close(){server.stop(0);executor.shutdownNow();}
}

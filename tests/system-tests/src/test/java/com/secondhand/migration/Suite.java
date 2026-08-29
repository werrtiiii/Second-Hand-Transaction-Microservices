package com.secondhand.migration;
import com.secondhand.micro.testing.*;
import com.secondhand.micro.user.UserApplication;
import com.secondhand.micro.product.ProductApplication;
import com.secondhand.micro.trade.TradeApplication;
import com.secondhand.micro.platform.Outbox;
import com.fasterxml.jackson.databind.*;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
/** 只启动三份独立数据库和真实 HTTP 服务；不启动原单体，也不替换业务实现。 */
public final class Suite {
 public static final ObjectMapper JSON=new ObjectMapper();
 public static final Suite INSTANCE=new Suite();
 public final TestEnvironment env=new TestEnvironment();
 public final Map<String,ServletWebServerApplicationContext> contexts=new HashMap<>();
 public final Map<String,String> urls=new HashMap<>();
 public final List<JsonNode> catalog=new ArrayList<>();
 public String currentCase="fixture";
 public final Set<String> covered=new TreeSet<>();
 public final List<Map<String,Object>> evidence=new ArrayList<>();
 private Suite(){try{
  JSON.readTree(getClass().getResourceAsStream("/api-catalog.json")).forEach(catalog::add);
  // 预留固定随机端口，让互相引用的地址在启动之前就已确定。
  for(String service:List.of("user","product","trade")){try(var socket=new ServerSocket(0)){urls.put(service,"http://127.0.0.1:"+socket.getLocalPort());}}
  for(String service:List.of("user","product","trade")){
   Class<?> app=switch(service){case "user"->UserApplication.class;case "product"->ProductApplication.class;default->TradeApplication.class;};
   contexts.put(service,env.start(service,app,Map.of("server.port",URI.create(urls.get(service)).getPort()+"","app.user-url",urls.get("user"),"app.product-url",urls.get("product"),"app.trade-url",urls.get("trade"))));
  }
  var registered=Http.call(urls.get("user"),"POST","/api/auth/register",Map.of("identityType","PHONE","identifier","13800000000","password","admin123"),null,null);
  assertEquals(201,registered.status());env.db("user").update("UPDATE users SET role='ADMIN' WHERE id=?",registered.data().path("userId").asLong());
  Runtime.getRuntime().addShutdownHook(new Thread(env::close));
 }catch(Exception e){env.close();throw new IllegalStateException(e);}}
 public JsonNode route(String method,String path){
  String pure=path.split("[?]")[0];return catalog.stream().filter(r->r.path("http_method").asText().equals(method)&&pure.matches(r.path("path").asText().replaceAll("[{][^}]+[}]","[^/]+")))
   .min(Comparator.comparingInt(r->(int)r.path("path").asText().chars().filter(c->c=='{').count())).orElseThrow(()->new IllegalArgumentException(method+" "+path));
 }
 public String endpoint(String method,String path){return urls.get(route(method,path).path("target_service").asText().replace("-service",""));}
 public HttpResponse<String> request(String method,String path,Object body,String token)throws Exception{
  var request=HttpRequest.newBuilder(URI.create(endpoint(method,path)+path)).timeout(java.time.Duration.ofSeconds(20)).header("Content-Type","application/json");
  if(token!=null)request.header("Authorization","Bearer "+token);
  if(method.equals("POST")&&path.equals("/api/orders"))request.header("Idempotency-Key",UUID.randomUUID().toString());
  request.method(method,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(body)));
  var result=HttpClient.newHttpClient().send(request.build(),HttpResponse.BodyHandlers.ofString());record(method,path,result.statusCode(),result.body());return result;
 }
 public void record(String method,String path,int status,String body)throws Exception{
  var r=route(method,path);boolean success=status>=200&&status<300&&JSON.readTree(body).path("success").asBoolean();
  evidence.add(Map.of("case",currentCase,"id",r.path("id").asText(),"method",method,"path",path,"status",status,"success",success));if(success)covered.add(r.path("id").asText());
 }
 public JdbcTemplate databaseFor(String sql){
  String lower=sql.toLowerCase();String service=lower.matches("(?s).*(from|join|update|into) +(users|user_identities|user_addresses|chat_messages|notifications)( |$).*")?"user":lower.matches("(?s).*(from|join|update|into) +(products|categories|product_images|comments|favorites|reports|inventory_reservations)( |$).*")?"product":"trade";
  return env.db(service);
 }
 public void flushEvents(){contexts.get("product").getBean(Outbox.class).deliver();contexts.get("trade").getBean(Outbox.class).deliver();}
 public void saveEvidence()throws Exception{Path dir=Path.of("target","api-coverage");Files.createDirectories(dir);JSON.writerWithDefaultPrettyPrinter().writeValue(dir.resolve("requests.json").toFile(),evidence);}
}

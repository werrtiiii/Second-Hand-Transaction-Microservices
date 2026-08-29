package com.secondhand.micro.system;
import com.secondhand.micro.testing.*;
import com.secondhand.micro.user.UserApplication;
import com.secondhand.micro.product.ProductApplication;
import com.secondhand.micro.trade.*;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.dao.DataAccessException;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class BusinessFlowIT {
 static TestEnvironment env;static String user,product,trade;static ResponseLossProxy proxy;static ServletWebServerApplicationContext tradeContext;
 @BeforeAll static void start()throws Exception{
  env=new TestEnvironment();user=TestEnvironment.http(env.start("user",UserApplication.class,Map.of()));
  product=TestEnvironment.http(env.start("product",ProductApplication.class,Map.of("app.user-url",user)));
  proxy=new ResponseLossProxy(product);startTrade();
 }
 static void startTrade(){tradeContext=env.start("trade",TradeApplication.class,Map.of("app.user-url",user,"app.product-url",proxy.url()));trade=TestEnvironment.http(tradeContext);}
 @AfterAll static void stop(){proxy.close();env.close();}
 String register(){var r=Http.call(user,"POST","/api/auth/register",Map.of("identityType","EMAIL","identifier",UUID.randomUUID()+"@example.com","password","password123"),null,null);assertEquals(201,r.status());return r.data().path("accessToken").asText();}
 long publish(String seller,int stock){var r=Http.call(product,"POST","/api/products",Map.of("title","真实跨服务测试商品","description","不使用跨服务数据库读写","priceCent",1500,"quantity",stock),seller,null);assertEquals(200,r.status());return r.data().path("id").asLong();}
 Map<String,Object> order(long productId){return Map.of("productId",productId,"receiverName","测试收件人","receiverPhone","13800000000","receiverAddress","测试地址");}
 int quantity(long id){return Http.call(product,"GET","/api/products/"+id,null,null,null).data().path("quantity").asInt();}
 @Test void registerPublishOrderAndCancelAcrossThreeServices(){
  String seller=register(),buyer=register();long productId=publish(seller,2);String key="flow-"+productId;
  var created=Http.call(trade,"POST","/api/orders",order(productId),buyer,key);assertEquals(200,created.status());long id=created.data().path("id").asLong();assertEquals("WAIT_PAY",created.data().path("status").asText());assertEquals(1,quantity(productId));
  var repeated=Http.call(trade,"POST","/api/orders",order(productId),buyer,key);assertEquals(id,repeated.data().path("id").asLong());assertEquals(1,quantity(productId));
  assertEquals(200,Http.call(trade,"POST","/api/orders/"+id+"/cancel",Map.of(),buyer,null).status());
  Http.call(trade,"POST","/api/orders/"+id+"/cancel",Map.of(),buyer,null);assertEquals(2,quantity(productId));
  assertEquals(1,env.db("trade").queryForObject("SELECT COUNT(*) FROM order_events WHERE order_id=? AND to_status='CANCELLED'",Integer.class,id));
 }
 @Test void foreignBuyerCannotReadOrCancelOrder(){
  String seller=register(),buyer=register(),stranger=register();long pid=publish(seller,1);
  long id=Http.call(trade,"POST","/api/orders",order(pid),buyer,"ownership-"+pid).data().path("id").asLong();
  assertEquals(403,Http.call(trade,"GET","/api/orders/"+id,null,stranger,null).status());
  assertEquals(404,Http.call(trade,"POST","/api/orders/"+id+"/cancel",Map.of(),stranger,null).status());assertEquals(0,quantity(pid));
 }
 @Test void lostReplyIsRecoveredAfterTradeServiceRestart(){
  String seller=register(),buyer=register();long pid=publish(seller,2);proxy.dropReservationReplies=true;
  long id;
  try{var result=Http.call(trade,"POST","/api/orders",order(pid),buyer,"lost-"+pid);assertEquals(202,result.status());id=result.data().path("id").asLong();assertEquals(1,quantity(pid));}
  finally{proxy.dropReservationReplies=false;}
  // 恢复任务来自交易数据库，不靠原 JVM 内存继续执行。
  tradeContext.close();startTrade();
  // 恢复采用最终一致性；允许瞬时冲突重试，但必须在期限内收敛到确认状态。
  org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(5)).untilAsserted(()->{
   tradeContext.getBean(TradeStore.class).recover();
   var result=Http.call(trade,"GET","/api/orders/"+id,null,buyer,null);
   assertEquals("WAIT_PAY",result.data().path("order").path("status").asText());assertEquals("CONFIRMED",env.db("trade").queryForObject("SELECT phase FROM trade_operations WHERE order_id=?",String.class,id));assertEquals(1,quantity(pid));
  });
 }
 @Test void databaseAccountsCannotReadOrWriteOtherServices(){
  Map<String,String> table=Map.of("user","users","product","products","trade","orders");
  for(String caller:table.keySet())for(String owner:table.keySet())if(!caller.equals(owner)){
   assertThrows(DataAccessException.class,()->env.db(caller).queryForList("SELECT * FROM secondhand_"+owner+"."+table.get(owner)+" LIMIT 1"));
   assertThrows(DataAccessException.class,()->env.db(caller).update("DELETE FROM secondhand_"+owner+"."+table.get(owner)+" WHERE 1=0"));
   assertThrows(DataAccessException.class,()->env.db(caller).update("UPDATE secondhand_"+owner+"."+table.get(owner)+" SET id=id WHERE 1=0"));
   assertThrows(DataAccessException.class,()->env.db(caller).update("INSERT INTO secondhand_"+owner+"."+table.get(owner)+"(id) SELECT -1 WHERE 1=0"));
  }
 }
 @Test void allServicesExposeDatabaseAwareReadiness(){
  // 就绪检查包含本服务数据库，存活检查不依赖下游服务。
  for(String endpoint:List.of(user,product,trade)){
   var result=Http.call(endpoint,"GET","/actuator/health/readiness",null,null,null);
   assertEquals(200,result.status());assertEquals("UP",result.body().path("status").asText());
  }
 }
 @Test void notificationLostReplyRetriesAfterSenderRecreationWithoutDuplicates()throws Exception{
  // 模拟真正接收成功后网络断开；所有发送状态与去重标识都持久化在所属库。
  long recipient=Http.call(user,"POST","/api/auth/register",Map.of("identityType","EMAIL","identifier",UUID.randomUUID()+"@example.com","password","password123"),null,null).data().path("userId").asLong();
  var db=env.db("trade");var json=tradeContext.getBean(com.fasterxml.jackson.databind.ObjectMapper.class);
  var remote=tradeContext.getBean(com.secondhand.micro.platform.Remote.class);
  try(var loss=new ResponseLossProxy(user)){
   loss.dropNotificationReplies=true;
   var first=new com.secondhand.micro.platform.Outbox(db,json,remote,loss.url());
   first.enqueue(recipient,"system",Map.of("content","可靠通知测试"));
   String event=db.queryForObject("SELECT id FROM outbox_events WHERE recipient_id=?",String.class,recipient);
   first.deliver();
   assertEquals(1,db.queryForObject("SELECT attempts FROM outbox_events WHERE id=?",Integer.class,event));
   assertNull(db.queryForObject("SELECT published_at FROM outbox_events WHERE id=?",java.sql.Timestamp.class,event));
   assertEquals(1,env.db("user").queryForObject("SELECT COUNT(*) FROM notifications WHERE id=?",Integer.class,"trade-service:"+event));
   assertTrue(db.queryForObject("SELECT next_attempt_at>NOW() FROM outbox_events WHERE id=?",Boolean.class,event));
   // 测试推进持久化重试时间，不在用例中等待真实退避窗口。
   db.update("UPDATE outbox_events SET next_attempt_at=NOW() WHERE id=?",event);loss.dropNotificationReplies=false;
   new com.secondhand.micro.platform.Outbox(db,json,remote,loss.url()).deliver();
   assertNotNull(db.queryForObject("SELECT published_at FROM outbox_events WHERE id=?",java.sql.Timestamp.class,event));
   assertEquals(1,env.db("user").queryForObject("SELECT COUNT(*) FROM notifications WHERE id=?",Integer.class,"trade-service:"+event));
  }
 }
}

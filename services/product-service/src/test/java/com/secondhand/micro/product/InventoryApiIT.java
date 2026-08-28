package com.secondhand.micro.product;
import com.secondhand.micro.testing.*;
import org.junit.jupiter.api.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
class InventoryApiIT {
 static TestEnvironment env;static String url,trade;
 @BeforeAll static void start(){env=new TestEnvironment();url=TestEnvironment.http(env.start("product",ProductApplication.class,Map.of()));trade=env.tokens("trade").serviceToken("product-service");}
 @AfterAll static void stop(){env.close();}
 long product(int quantity){
  env.db("product").update("INSERT INTO products(seller_id,title,description,price_cent,quantity,status,version) VALUES(2,'测试商品','描述',1000,?,'ON_SALE',0)",quantity);
  return env.db("product").queryForObject("SELECT MAX(id) FROM products",Long.class);
 }
 Map<String,Object> reserve(String key,long order,long product){return Map.of("operationId",key,"orderId",order,"productId",product,"buyerId",1,"expectedSellerId",2,"quantity",1,"expectedProductVersion",0,"pricingMode","LIST","expectedListPriceCent",1000);}
 Http.Result call(String method,String path,Object body,String key){return Http.call(url,method,"/internal/v1/inventory/reservations"+path,body,env.tokens("trade").serviceToken("product-service"),key);}
 int stock(long id){return env.db("product").queryForObject("SELECT quantity FROM products WHERE id=?",Integer.class,id);}
 @Test void duplicateReserveAndReleaseChangeStockOnlyOnce(){
  long id=product(2);String key="duplicate-"+id;var payload=reserve(key,id,id);
  assertEquals(200,call("POST","",payload,key).status());assertEquals(200,call("POST","",payload,key).status());assertEquals(1,stock(id));
  assertEquals(200,call("POST","/"+key+"/confirm",Map.of("orderId",id),null).status());assertEquals(1,stock(id));
  call("POST","/"+key+"/release",Map.of("orderId",id),null);call("POST","/"+key+"/release",Map.of("orderId",id),null);assertEquals(2,stock(id));
 }
 @Test void sameKeyWithDifferentPayloadIsConflict(){
  long id=product(2);String key="conflict-"+id;var payload=reserve(key,id,id);call("POST","",payload,key);
  var altered=new HashMap<>(payload);altered.put("buyerId",99);
  assertEquals(409,call("POST","",altered,key).status());assertEquals(1,stock(id));
 }
 @Test void releaseBeforeReserveLeavesTerminalTombstone(){
  // 模拟取消先到、预占请求后到的网络乱序。
  long id=product(1);String key="late-"+id;
  assertEquals(200,call("POST","/"+key+"/release",Map.of("orderId",id),null).status());
  assertEquals(409,call("POST","",reserve(key,id,id),key).status());assertEquals(1,stock(id));
 }
 @Test void concurrentBuyersCannotOversell()throws Exception{
  long id=product(1);var executor=Executors.newFixedThreadPool(6);
  try{
   List<Callable<Integer>> work=new ArrayList<>();
   for(int i=0;i<6;i++){int n=i;work.add(()->call("POST","",reserve("race-"+id+"-"+n,id*100+n+1000,id),"race-"+id+"-"+n).status());}
   var results=executor.invokeAll(work);long successes=0;
   for(var result:results){int status=result.get();assertTrue(status==200||status==409);if(status==200)successes++;}
   assertEquals(1,successes);assertEquals(0,stock(id));
  }finally{executor.shutdownNow();}
 }
 @Test void releaseDoesNotUndoAdministrativeOffShelf(){
  long id=product(1);String key="moderated-"+id;call("POST","",reserve(key,id,id),key);
  env.db("product").update("UPDATE products SET off_shelf_reason='MODERATION',status='OFF_SALE' WHERE id=?",id);
  call("POST","/"+key+"/release",Map.of("orderId",id),null);
  assertEquals(1,stock(id));assertEquals("OFF_SALE",env.db("product").queryForObject("SELECT status FROM products WHERE id=?",String.class,id));
 }
 @Test void internalApiRequiresCorrectServiceAndAudience(){
  var payload=reserve("denied",9999,9999);
  assertEquals(401,Http.call(url,"POST","/internal/v1/inventory/reservations",payload,null,"denied").status());
  assertEquals(403,Http.call(url,"POST","/internal/v1/inventory/reservations",payload,env.tokens("user").serviceToken("product-service"),"denied").status());
  assertEquals(401,Http.call(url,"POST","/internal/v1/inventory/reservations",payload,env.tokens("trade").serviceToken("user-service"),"denied").status());
 }
 @Test void bindingAndQuantityAreValidated(){
  long id=product(1);String key="binding-"+id;call("POST","",reserve(key,id,id),key);
  assertEquals(409,call("POST","/"+key+"/release",Map.of("orderId",id+999),null).status());
  var invalid=new HashMap<>(reserve("invalid-"+id,id+999,id));invalid.put("quantity",0);
  assertEquals(400,call("POST","",invalid,"invalid-"+id).status());assertEquals(0,stock(id));
 }
 @Test void secondOperationCannotClaimSameOrder(){
  long id=product(2);call("POST","",reserve("first-"+id,id,id),"first-"+id);
  assertEquals(409,call("POST","",reserve("second-"+id,id,id),"second-"+id).status());assertEquals(1,stock(id));
 }
 @Test void concurrentDuplicateReservationsAllReturnSameResult()throws Exception{
  // 多个线程同时重放同一业务键，全部成功且库存只扣一次。
  long id=product(2);String key="same-race-"+id;var executor=Executors.newFixedThreadPool(6);var barrier=new CyclicBarrier(6);
  try{
   List<Callable<Http.Result>> work=new ArrayList<>();
   for(int i=0;i<6;i++)work.add(()->{barrier.await(5,TimeUnit.SECONDS);return call("POST","",reserve(key,id,id),key);});
   for(var future:executor.invokeAll(work)){var result=future.get();assertEquals(200,result.status());assertEquals(key,result.data().path("reservationId").asText());}
   assertEquals(1,stock(id));assertEquals(1,env.db("product").queryForObject("SELECT COUNT(*) FROM inventory_reservations WHERE operation_id=?",Integer.class,key));
  }finally{executor.shutdownNow();}
 }
}

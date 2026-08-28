package com.secondhand.micro.trade;
import com.secondhand.micro.testing.*;
import org.junit.jupiter.api.*;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
class TradeApiIT {
 static TestEnvironment env;static String url;
 @BeforeAll static void start(){env=new TestEnvironment();url=TestEnvironment.http(env.start("trade",TradeApplication.class,Map.of()));}
 @AfterAll static void stop(){env.close();}
 @Test void anonymousOrderCreationIsRejected(){
  // 不连接用户服务也不能放行匿名写入。
  assertEquals(401,Http.call(url,"POST","/api/orders",Map.of("productId",1,"receiverName","测试","receiverPhone","13800000000","receiverAddress","测试地址"),null,"anonymous").status());
  assertEquals(0,env.db("trade").queryForObject("SELECT COUNT(*) FROM orders",Integer.class));
 }
 @Test void invalidReceiverIsRejectedBeforeSideEffects(){assertEquals(400,Http.call(url,"POST","/api/orders",Map.of("productId",1,"receiverName","","receiverPhone","","receiverAddress",""),null,"invalid").status());}
 @Test void internalOrderStateRequiresProductService(){
  assertEquals(401,Http.call(url,"GET","/internal/v1/orders/1/inventory-state",null,null,null).status());
  assertEquals(403,Http.call(url,"GET","/internal/v1/orders/1/inventory-state",null,env.tokens("user").serviceToken("trade-service"),null).status());
 }
}

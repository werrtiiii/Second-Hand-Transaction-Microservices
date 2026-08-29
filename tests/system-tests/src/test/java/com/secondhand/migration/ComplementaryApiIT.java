package com.secondhand.migration;
import com.secondhand.testutil.ApiIntegrationTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.net.*;
import java.net.http.*;
import java.io.*;
import static org.assertj.core.api.Assertions.assertThat;
/** 补齐原七类用例未覆盖的公开接口，逐次检查成功结果和关键业务字段。 */
class ComplementaryApiIT extends ApiIntegrationTestBase {
 @Test void profilesAddressesAndAddressSnapshot()throws Exception{
  Actor buyer=user(),seller=user();
  ok("POST","/api/auth/heartbeat",null,buyer);
  assertThat(ok("GET","/api/users/profile",null,buyer).path("userId").asLong()).isEqualTo(buyer.id());
  assertThat(ok("PUT","/api/users/profile",Map.of("nickname","迁移测试用户"),buyer).path("nickname").asText()).isEqualTo("迁移测试用户");
  assertThat(ok("GET","/api/users/"+buyer.id()+"/public",null,null).path("nickname").asText()).isEqualTo("迁移测试用户");
  var address=Map.of("receiverName","地址收件人","receiverPhone","13900000000","province","浙江省","city","杭州市","district","西湖区","detailAddress","测试路1号","isDefault",true,"tag","家");
  long id=ok("POST","/api/users/addresses",address,buyer).path("id").asLong();assertThat(id).isPositive();
  assertThat(ok("GET","/api/users/addresses",null,buyer).size()).isEqualTo(1);
  assertThat(ok("PUT","/api/users/addresses/"+id,address,buyer).path("receiverName").asText()).isEqualTo("地址收件人");
  assertThat(ok("PUT","/api/users/addresses/"+id+"/default",null,buyer).path("isDefault").asBoolean()).isTrue();
  long pid=product(seller,2);long order=ok("POST","/api/orders",Map.of("productId",pid,"addressId",id),buyer).path("id").asLong();
  assertThat(ok("GET","/api/orders/"+order,null,buyer).path("order").path("receiverAddress").asText()).contains("测试路1号");
  assertThat(call("POST","/api/orders",Map.of("productId",pid,"addressId",id),seller).getResponse().getStatus()).isIn(403,404);
  ok("DELETE","/api/users/addresses/"+id,null,buyer);assertThat(ok("GET","/api/users/addresses",null,buyer)).isEmpty();
  assertThat(ok("GET","/api/regions",null,null).size()).isGreaterThan(0);
  String avatar=upload("PUT","/api/users/avatar",buyer).asText();assertThat(avatar).startsWith("/uploads/avatars/");
  ok("POST","/api/auth/password/change",Map.of("oldPassword",PASSWORD,"newPassword","changed-password123"),buyer);
  assertThat(call("GET","/api/users/profile",null,buyer).getResponse().getStatus()).isEqualTo(401);
  assertThat(ok("POST","/api/auth/login",credentials("PHONE",buyer.identifier(),"changed-password123"),null).path("userId").asLong()).isEqualTo(buyer.id());
 }
 @Test void commentsFavoritesChatAndNotificationProjection()throws Exception{
  Actor seller=user(),buyer=user();long pid=product(seller,2);
  assertThat(ok("GET","/api/categories",null,null).size()).isPositive();
  assertThat(ok("GET","/api/my-products",null,seller).path("totalElements").asInt()).isEqualTo(1);
  assertThat(ok("GET","/api/users/"+seller.id()+"/products",null,null).path("totalElements").asInt()).isEqualTo(1);
  long comment=ok("POST","/api/products/"+pid+"/comments",Map.of("content","还能优惠吗"),buyer).path("id").asLong();assertThat(comment).isPositive();
  assertThat(ok("GET","/api/products/"+pid+"/comments",null,null).size()).isEqualTo(1);
  ok("POST","/api/products/"+pid+"/favorite",null,buyer);
  assertThat(ok("GET","/api/products/"+pid+"/favorite/status",null,buyer).toString()).contains("true");
  assertThat(ok("GET","/api/users/favorites",null,buyer).size()).isPositive();
  ok("DELETE","/api/products/"+pid+"/favorite",null,buyer);
  assertThat(ok("POST","/api/products/"+pid+"/chat",Map.of("receiverId",seller.id(),"content","准备购买"),buyer).path("id").asLong()).isPositive();
  assertThat(ok("GET","/api/products/"+pid+"/chat?with="+buyer.id(),null,seller).size()).isEqualTo(1);
  assertThat(ok("GET","/api/users/messages",null,seller).size()).isEqualTo(1);
  ok("PUT","/api/messages/read",Map.of("productId",pid,"otherUserId",buyer.id()),seller);
  assertThat(ok("GET","/api/users/notifications",null,seller).path("unreadMessages").asLong()).isZero();
  Suite.INSTANCE.flushEvents();assertThat(ok("GET","/api/messages/comments",null,seller).get(0).path("content").asText()).isEqualTo("还能优惠吗");
  ok("GET","/api/messages/system",null,seller);
 }
 @Test void productImagesAreBoundToOwnedProduct()throws Exception{
  Actor seller=user(),other=user();long pid=product(seller,1),foreign=product(other,1);
  long image=upload("POST","/api/products/"+pid+"/images",seller).path("id").asLong();assertThat(image).isPositive();
  assertThat(ok("GET","/api/products/"+pid+"/images",null,seller).size()).isEqualTo(1);
  ok("PUT","/api/products/"+pid+"/images/"+image+"/cover",null,seller);
  assertThat(call("DELETE","/api/products/"+foreign+"/images/"+image,null,other).getResponse().getStatus()).isIn(403,404);
  assertThat(ok("GET","/api/products/"+pid+"/images",null,seller).size()).isEqualTo(1);
  ok("DELETE","/api/products/"+pid+"/images/"+image,null,seller);
  assertThat(ok("GET","/api/products/"+pid+"/images",null,seller)).isEmpty();
 }
 @Test void adminQueriesAndModerationRespectRoles()throws Exception{
  Actor admin=admin(),seller=user(),buyer=user();long pid=product(seller,1);
  assertThat(ok("GET","/api/admin/dashboard",null,admin).path("totalUsers").asLong()).isPositive();
  for(String path:List.of("/api/admin/users","/api/admin/users/online","/api/admin/users/"+seller.id(),"/api/admin/products","/api/admin/reports","/api/admin/orders","/api/admin/after-sale"))ok("GET",path,null,admin);
  assertThat(ok("PUT","/api/admin/products/"+pid+"/off-shelf",null,admin).path("status").asText()).isEqualTo("OFF_SALE");
  assertThat(ok("PUT","/api/admin/products/"+pid+"/on-shelf",null,admin).path("status").asText()).isEqualTo("ON_SALE");
  ok("DELETE","/api/admin/products/"+pid,null,admin);
  assertThat(call("GET","/api/products/"+pid,null,null).getResponse().getStatus()).isEqualTo(404);
  ok("POST","/api/admin/users/"+buyer.id()+"/kick",null,admin);assertThat(call("GET","/api/users/profile",null,buyer).getResponse().getStatus()).isEqualTo(401);
  assertThat(call("GET","/api/admin/dashboard",null,seller).getResponse().getStatus()).isEqualTo(403);
 }
 @Test void paymentOwnershipPersistenceAndOrderLists()throws Exception{
  Trade t=trade();Actor stranger=user();
  String no=ok("POST","/api/payments",Map.of("orderId",t.orderId(),"method","ALIPAY"),t.buyer()).path("paymentNo").asText();assertThat(no).startsWith("PAY-");
  assertThat(ok("GET","/api/payments/"+no,null,t.buyer()).asText()).isEqualTo("WAIT_PAY");
  assertThat(call("POST","/api/payments/"+no+"/mock-pay?orderId="+t.orderId(),null,stranger).getResponse().getStatus()).isEqualTo(404);
  ok("POST","/api/payments/"+no+"/mock-pay?orderId="+t.orderId(),null,t.buyer());
  ok("POST","/api/payments/"+no+"/mock-pay?orderId="+t.orderId(),null,t.buyer());
  assertThat(ok("GET","/api/payments/"+no,null,t.buyer()).asText()).isEqualTo("PAID");orderStatus(t.orderId(),"WAIT_DELIVER");
  assertThat(count("SELECT COUNT(*) FROM payments WHERE order_id=?",t.orderId())).isEqualTo(1);
  assertThat(ok("GET","/api/orders/bought",null,t.buyer()).path("totalElements").asLong()).isEqualTo(1);
  assertThat(ok("GET","/api/orders/sold",null,t.seller()).path("totalElements").asLong()).isEqualTo(1);
  Trade legacy=trade();ok("POST","/api/orders/"+legacy.orderId()+"/mark-paid",null,legacy.buyer());orderStatus(legacy.orderId(),"WAIT_DELIVER");
  Trade manual=trade();Actor admin=admin();ok("POST","/api/admin/orders/"+manual.orderId()+"/mark-paid",null,admin);
  assertThat(ok("GET","/api/admin/orders/"+manual.orderId(),null,admin).path("order").path("status").asText()).isEqualTo("WAIT_DELIVER");
  Trade cancelled=trade();ok("POST","/api/admin/orders/"+cancelled.orderId()+"/cancel",null,admin);stock(cancelled.productId(),1,"ON_SALE");
 }
 @Test void ratingsAndSettlements()throws Exception{
  Trade t=completedTrade();
  assertThat(ok("POST","/api/orders/"+t.orderId()+"/rate",Map.of("score",5,"comment","物品符合描述"),t.buyer()).path("score").asInt()).isEqualTo(5);
  assertThat(ok("GET","/api/orders/"+t.orderId()+"/rating",null,t.buyer()).path("score").asInt()).isEqualTo(5);
  assertThat(ok("GET","/api/users/"+t.seller().id()+"/rating",null,null).path("averageScore").asDouble()).isEqualTo(5);
  assertThat(ok("GET","/api/users/"+t.seller().id()+"/sold",null,null).size()).isEqualTo(1);
  databaseFor("UPDATE orders").update("UPDATE orders SET completed_at=DATE_SUB(NOW(),INTERVAL 8 DAY) WHERE id=?",t.orderId());
  assertThat(ok("POST","/api/orders/process-settlements",null,admin()).asInt()).isGreaterThanOrEqualTo(1);orderStatus(t.orderId(),"SETTLED");
 }
 @Test void offerQueriesAndReceiverCompletion()throws Exception{
  Actor seller=user(),buyer=user();long pid=product(seller,2);
  long offer=ok("POST","/api/products/"+pid+"/offers",Map.of("offeredPriceCent",800,"message","可以吗"),buyer).path("id").asLong();
  assertThat(ok("GET","/api/products/"+pid+"/offers",null,seller).size()).isEqualTo(1);
  assertThat(ok("GET","/api/my-offers",null,buyer).size()).isEqualTo(1);assertThat(ok("GET","/api/seller-offers",null,seller).size()).isEqualTo(1);
  long order=ok("POST","/api/offers/"+offer+"/accept",null,seller).path("id").asLong();
  assertThat(ok("PUT","/api/orders/"+order+"/receiver",receiver(),buyer).path("receiverName").asText()).isEqualTo("测试买家");
  assertThat(ok("POST","/api/offers/"+offer+"/accept",null,seller).path("id").asLong()).isEqualTo(order);
  assertThat(count("SELECT COUNT(*) FROM trade_operations WHERE idempotency_key=?","offer-accept-"+offer)).isEqualTo(1);
 }
 @Test void afterSaleEvidenceQueriesReturnDisputeAndLegacyAdminRoutes()throws Exception{
  Trade t=completedTrade();Actor admin=admin();
  long id=ok("POST","/api/after-sale",Map.of("orderId",t.orderId(),"type","RETURN_REFUND","reason","退货","refundAmountCent",1000),t.buyer()).path("id").asLong();
  for(String path:List.of("/api/after-sale/my-requests","/api/after-sale/by-order/"+t.orderId(),"/api/after-sale/"+id))ok("GET",path,null,t.buyer());
  ok("GET","/api/after-sale/my-received",null,t.seller());ok("GET","/api/after-sale/all",null,admin);ok("GET","/api/admin/after-sale/"+id,null,admin);
  assertThat(ok("POST","/api/after-sale/"+id+"/buyer-evidence",Map.of("evidence","开箱图片"),t.buyer()).path("buyerEvidence").asText()).contains("开箱图片");
  assertThat(ok("POST","/api/after-sale/"+id+"/seller-evidence",Map.of("evidence","出库录像"),t.seller()).path("sellerEvidence").asText()).contains("出库录像");
  ok("POST","/api/after-sale/"+id+"/approve",null,t.seller());ok("POST","/api/after-sale/"+id+"/return-ship",Map.of("carrierCode","SF","trackingNo","RETURN-COVERAGE"),t.buyer());
  assertThat(ok("POST","/api/after-sale/"+id+"/reject-return",Map.of("note","货物破损"),t.seller()).path("status").asText()).isEqualTo("REJECTED");
  assertThat(ok("POST","/api/after-sale/"+id+"/escalate",Map.of("evidence","退货争议"),t.buyer()).path("status").asText()).isEqualTo("PLATFORM_ARBITRATION");
  assertThat(ok("POST","/api/after-sale/"+id+"/arbitrate",Map.of("result","FULL_REFUND","note","裁定退款"),admin).path("status").asText()).isEqualTo("REFUNDED");
  assertThat(count("SELECT COUNT(*) FROM refunds WHERE after_sale_id=?",id)).isEqualTo(1);
  ok("POST","/api/after-sale/process-timeouts",null,admin);ok("POST","/api/admin/after-sale/process-timeouts",null,admin);
  assertThat(call("GET","/api/after-sale/all",null,t.buyer()).getResponse().getStatus()).isEqualTo(403);
 }
 private JsonNode upload(String method,String path,Actor actor)throws Exception{
  // 小图片由测试生成，不依赖网络文件；实际请求使用 multipart/form-data。
  String boundary="test-"+UUID.randomUUID();var bytes=new ByteArrayOutputStream();
  bytes.write(("--"+boundary+"\r\nContent-Disposition: form-data; name=\"file\"; filename=\"fixture.png\"\r\nContent-Type: image/png\r\n\r\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
  javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(2,2,java.awt.image.BufferedImage.TYPE_INT_RGB),"png",bytes);bytes.write(("\r\n--"+boundary+"--\r\n").getBytes());
  var req=HttpRequest.newBuilder(URI.create(Suite.INSTANCE.endpoint(method,path)+path)).header("Authorization","Bearer "+actor.token()).header("Content-Type","multipart/form-data; boundary="+boundary).method(method,HttpRequest.BodyPublishers.ofByteArray(bytes.toByteArray())).build();
  var res=HttpClient.newHttpClient().send(req,HttpResponse.BodyHandlers.ofString());Suite.INSTANCE.record(method,path,res.statusCode(),res.body());
  assertThat(res.statusCode()).as(res.body()).isEqualTo(200);var json=mapper.readTree(res.body());assertThat(json.path("success").asBoolean()).as(res.body()).isTrue();return json.path("data");
 }
}

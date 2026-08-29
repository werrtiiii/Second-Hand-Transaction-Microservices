package com.secondhand.micro.trade;
import com.secondhand.micro.platform.*;
import com.fasterxml.jackson.databind.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import java.sql.Statement;
import java.util.*;
@Service
public class TradeStore {
 @org.springframework.beans.factory.annotation.Autowired private Outbox outbox;
 @org.springframework.beans.factory.annotation.Value("${app.user-url}") private String userUrl;
 private final String worker=UUID.randomUUID().toString();
 private final JdbcTemplate db; private final TransactionTemplate tx; private final Remote remote; private final String productUrl; private final ObjectMapper json;
 public TradeStore(JdbcTemplate db,PlatformTransactionManager manager,Remote remote,@Value("${app.product-url}") String url,ObjectMapper json){this.db=db;this.tx=new TransactionTemplate(manager);this.remote=remote;productUrl=url;this.json=json;}
 private String encode(Object o){try{return json.writeValueAsString(o);}catch(Exception e){throw new IllegalStateException(e);}}
 public Map<String,Object> create(long buyer,String key,TradeController.Create request){
  Hashes.key(key);String hash=Hashes.sha256(encode(request));
  var existing=db.queryForList("SELECT * FROM trade_operations WHERE actor_id=? AND idempotency_key=?",buyer,key);
  if(!existing.isEmpty()){if(!hash.equals(existing.get(0).get("payload_hash")))throw Failure.conflict("同键请求内容不同");long id=((Number)existing.get(0).get("order_id")).longValue();advance(id);return get(buyer,id);}
  // 只读查询失败时还未扣库存；建立持久化操作后才执行远程写入。
  var product=remote.get(productUrl,"product-service","/internal/v1/products/"+request.productId());
  if(product.path("sellerId").asLong()==buyer)throw Failure.forbidden();
  if(!"ON_SALE".equals(product.path("status").asText())||product.path("quantity").asInt()<1)throw Failure.conflict("商品已下架或售罄");
  int amount=product.path("priceCent").asInt();
  if(request.offerId()!=null){
   var offers=db.queryForList("SELECT * FROM offers WHERE id=? AND buyer_id=? AND product_id=? AND status IN ('ACCEPTING','ACCEPTED')",request.offerId(),buyer,request.productId());
   if(offers.isEmpty())throw Failure.forbidden();amount=((Number)offers.get(0).get("offered_price_cent")).intValue();
  }
  String name=request.receiverName(),phone=request.receiverPhone(),address=request.receiverAddress();
  if(request.addressId()!=null){var a=remote.post(userUrl,"user-service","/internal/v1/addresses/resolve",Map.of("userId",buyer,"addressId",request.addressId()),null);name=a.path("receiver_name").asText();phone=a.path("receiver_phone").asText();address=a.path("province").asText()+" "+a.path("city").asText()+" "+a.path("district").asText()+" "+a.path("detail_address").asText();}
  final int price=amount;final String receiver=name,receiverPhone=phone,receiverAddress=address;
  long id=tx.execute(status->{
   db.update("INSERT INTO trade_operations(actor_id,idempotency_key,payload_hash,phase,created_at,updated_at) VALUES(?,?,?,'NEW',NOW(),NOW()) ON DUPLICATE KEY UPDATE id=trade_operations.id",buyer,key,hash);
   var operation=db.queryForMap("SELECT * FROM trade_operations WHERE actor_id=? AND idempotency_key=? FOR UPDATE",buyer,key);
   if(!hash.equals(operation.get("payload_hash")))throw Failure.conflict("同键请求内容不同");
   if(operation.get("order_id")!=null)return ((Number)operation.get("order_id")).longValue();
   var keys=new GeneratedKeyHolder();
   db.update(c->{var p=c.prepareStatement("INSERT INTO orders(buyer_id,seller_id,product_id,amount_cent,status,receiver_name,receiver_phone,receiver_address,product_title,product_version,created_at,updated_at) VALUES(?,?,?,?,'CREATING',?,?,?,?,?,NOW(),NOW())",Statement.RETURN_GENERATED_KEYS);
    p.setLong(1,buyer);p.setLong(2,product.path("sellerId").asLong());p.setLong(3,request.productId());p.setInt(4,price);p.setString(5,receiver);p.setString(6,receiverPhone);p.setString(7,receiverAddress);p.setString(8,product.path("title").asText());p.setLong(9,product.path("version").asLong());return p;},keys);
   long order=keys.getKey().longValue();
   db.update("UPDATE orders SET list_price_cent=?,address_id=? WHERE id=?",product.path("priceCent").asInt(),request.addressId(),order);
   db.update("UPDATE trade_operations SET order_id=?,phase='RESERVE_PENDING',updated_at=NOW() WHERE id=?",order,operation.get("id"));
   return order;
  });
  advance(id);return get(buyer,id);
 }
 public Map<String,Object> get(long buyer,long order){
  var rows=db.queryForList("SELECT o.*,t.phase AS inventory_phase FROM orders o JOIN trade_operations t ON t.order_id=o.id WHERE o.id=? AND o.buyer_id=?",order,buyer);
  if(rows.isEmpty())throw Failure.missing();return view(rows.get(0));
 }
 private Map<String,Object> view(Map<String,Object> r){
  var result=new LinkedHashMap<String,Object>();
  result.put("id",r.get("id"));result.put("buyerId",r.get("buyer_id"));result.put("sellerId",r.get("seller_id"));result.put("productId",r.get("product_id"));result.put("amountCent",r.get("amount_cent"));result.put("status",r.get("status"));result.put("productTitle",r.get("product_title"));result.put("inventoryPhase",r.get("inventory_phase"));return result;
 }
 public Map<String,Object> cancel(long buyer,long order){
  tx.executeWithoutResult(status->{
   var rows=db.queryForList("SELECT * FROM orders WHERE id=? AND buyer_id=? FOR UPDATE",order,buyer);
   if(rows.isEmpty())throw Failure.missing();String current=(String)rows.get(0).get("status");
   if("CANCELLED".equals(current))return;
   if(!Set.of("WAIT_PAY","CREATING").contains(current))throw Failure.conflict("当前状态不能取消");
   db.update("UPDATE orders SET version=version+1,status='CANCELLED',cancelled_at=NOW(),updated_at=NOW() WHERE id=?",order);
   db.update("UPDATE trade_operations SET phase='RELEASE_PENDING',updated_at=NOW() WHERE order_id=?",order);
   event(order,current,"CANCELLED","买家取消；库存补偿已持久化");
  });
  advance(order);return get(buyer,order);
 }
 private void event(long id,String from,String to,String note){
  db.update("INSERT INTO order_events(order_id,from_status,to_status,note,created_at) VALUES(?,?,?,?,NOW())",id,from,to,note);
  var o=db.queryForMap("SELECT buyer_id,seller_id FROM orders WHERE id=?",id);
  var message=Map.of("id","order-"+id+"-"+to,"type","order_event","title","订单 #"+id,"content",note,"relatedId",String.valueOf(id),"time",java.time.LocalDateTime.now().toString());
  outbox.enqueue(((Number)o.get("buyer_id")).longValue(),"system",message);outbox.enqueue(((Number)o.get("seller_id")).longValue(),"system",message);
 }
 public void advance(long order){
  var rows=db.queryForList("SELECT o.*,t.phase AS inventory_phase FROM orders o JOIN trade_operations t ON t.order_id=o.id WHERE o.id=?",order);
  if(rows.isEmpty())return;var o=rows.get(0);String phase=(String)o.get("inventory_phase");String operation="order-create-"+order;
  try{
   if("RESERVE_PENDING".equals(phase)){
    var payload=new LinkedHashMap<String,Object>();
    payload.put("operationId",operation);payload.put("orderId",order);payload.put("productId",o.get("product_id"));payload.put("buyerId",o.get("buyer_id"));payload.put("expectedSellerId",o.get("seller_id"));payload.put("quantity",1);payload.put("expectedProductVersion",o.get("product_version"));payload.put("pricingMode","LIST");payload.put("expectedListPriceCent",o.get("list_price_cent")==null?o.get("amount_cent"):o.get("list_price_cent"));
    remote.post(productUrl,"product-service","/internal/v1/inventory/reservations",payload,operation);
    tx.executeWithoutResult(s->{
     int changed=db.update("UPDATE orders SET version=version+1,status='WAIT_PAY',updated_at=NOW() WHERE id=? AND status='CREATING'",order);
     if(changed==1){db.update("UPDATE trade_operations SET phase='CONFIRM_PENDING',last_error=NULL,updated_at=NOW() WHERE order_id=? AND phase='RESERVE_PENDING'",order);event(order,"CREATING","WAIT_PAY","库存已预占，等待支付");}
    });
    // 重新读取阶段，取消并发发生后绝不能再把订单推进待支付。
    advance(order);return;
   }
   if("CONFIRM_PENDING".equals(phase)||"RELEASE_PENDING".equals(phase)){
    boolean release="RELEASE_PENDING".equals(phase);
    remote.post(productUrl,"product-service","/internal/v1/inventory/reservations/"+operation+(release?"/release":"/confirm"),Map.of("orderId",order),operation+(release?":release":":confirm"));
    db.update("UPDATE trade_operations SET phase=?,last_error=NULL,updated_at=NOW() WHERE order_id=? AND phase=?",release?"RELEASED":"CONFIRMED",order,phase);
   }
  }catch(Failure failure){
   if("RESERVE_PENDING".equals(phase)&&Set.of(400,403,404,409).contains(failure.status)){
    tx.executeWithoutResult(s->{
     int changed=db.update("UPDATE orders SET version=version+1,status='CREATE_FAILED',updated_at=NOW() WHERE id=? AND status='CREATING'",order);
     if(changed==1){db.update("UPDATE trade_operations SET phase='FAILED',last_error=?,updated_at=NOW() WHERE order_id=? AND phase='RESERVE_PENDING'",failure.code,order);event(order,"CREATING","CREATE_FAILED","商品服务拒绝预占");}
    });
   }else{
    // 网络故障不吞掉恢复任务；固定业务键重试，进程重启后仍可恢复。
    db.update("UPDATE trade_operations SET attempts=attempts+1,last_error=?,next_attempt_at=DATE_ADD(NOW(),INTERVAL LEAST(300,POW(2,LEAST(attempts,8))) SECOND),updated_at=NOW() WHERE order_id=? AND phase=?",failure.code,order,phase);
   }
  }
 }
 @Scheduled(fixedDelayString="${app.recovery-delay-ms:5000}")
 public void recover(){
  var ids=db.queryForList("SELECT order_id FROM trade_operations WHERE phase IN ('RESERVE_PENDING','CONFIRM_PENDING','RELEASE_PENDING') AND order_id IS NOT NULL AND next_attempt_at<=NOW() AND (lease_until IS NULL OR lease_until<NOW()) ORDER BY next_attempt_at LIMIT 50",Long.class);
  for(long id:ids){
   if(db.update("UPDATE trade_operations SET lease_owner=?,lease_until=DATE_ADD(NOW(),INTERVAL 30 SECOND) WHERE order_id=? AND (lease_until IS NULL OR lease_until<NOW())",worker,id)!=1)continue;
   try{advance(id);}catch(Exception e){org.slf4j.LoggerFactory.getLogger(getClass()).warn("库存恢复失败 orderId={} cause={}",id,e.getClass().getSimpleName());}
   finally{db.update("UPDATE trade_operations SET lease_until=NULL WHERE order_id=? AND lease_owner=?",id,worker);}
  }
 }
 public Map<String,Object> inventoryState(long order){
  var rows=db.queryForList("SELECT o.status,t.phase FROM orders o JOIN trade_operations t ON t.order_id=o.id WHERE o.id=?",order);
  if(rows.isEmpty())throw Failure.missing();
  return Map.of("orderId",order,"operationId","order-create-"+order,"orderStatus",rows.get(0).get("status"),"inventoryAction",rows.get(0).get("phase"));
 }
}

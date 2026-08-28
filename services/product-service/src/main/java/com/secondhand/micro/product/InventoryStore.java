package com.secondhand.micro.product;
import com.secondhand.micro.platform.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service
public class InventoryStore {
 private final JdbcTemplate db; private final ProductStore products; private final ObjectMapper json;
 public InventoryStore(JdbcTemplate db,ProductStore products,ObjectMapper json){this.db=db;this.products=products;this.json=json;}
 private String encode(Object value){try{return json.writeValueAsString(value);}catch(Exception e){throw new IllegalStateException(e);}}
 public Map<String,Object> get(String operation){
  var rows=db.queryForList("SELECT * FROM inventory_reservations WHERE operation_id=?",operation);
  if(rows.isEmpty())throw Failure.missing();return view(rows.get(0));
 }
 private Map<String,Object> lock(String operation){var rows=db.queryForList("SELECT * FROM inventory_reservations WHERE operation_id=? FOR UPDATE",operation);if(rows.isEmpty())throw Failure.conflict("订单已绑定其他库存操作");return rows.get(0);}
 private Map<String,Object> view(Map<String,Object> row){
  var result=new LinkedHashMap<String,Object>();result.put("reservationId",row.get("operation_id"));result.put("status",row.get("status"));result.put("orderId",row.get("order_id"));result.put("quantity",row.get("quantity"));
  if(row.get("product_snapshot")!=null){try{result.put("productSnapshot",json.readTree((String)row.get("product_snapshot")));}catch(Exception e){throw new IllegalStateException(e);}}
  return result;
 }
 @Transactional
 public Map<String,Object> reserve(InventoryController.Reserve r,String key){
  Hashes.key(key);if(!key.equals(r.operationId()))throw Failure.conflict("幂等键与操作号不一致");
  String hash=Hashes.sha256(encode(r));
  // 重复键直接取得排他锁，避免 INSERT IGNORE 的共享锁升级导致并发重试死锁。
  db.update("INSERT INTO inventory_reservations(operation_id,order_id,product_id,quantity,status,payload_hash,created_at,updated_at) VALUES(?,?,?,?,'RESERVING',?,NOW(),NOW()) ON DUPLICATE KEY UPDATE operation_id=inventory_reservations.operation_id",r.operationId(),r.orderId(),r.productId(),r.quantity(),hash);
  var reservation=lock(r.operationId());
  if("RELEASED".equals(reservation.get("status")))throw Failure.conflict("操作已释放，拒绝迟到预占");
  if(!hash.equals(reservation.get("payload_hash")))throw Failure.conflict("同一幂等键不能更改请求");
  if(!"RESERVING".equals(reservation.get("status")))return view(reservation);
  var found=db.queryForList("SELECT * FROM products WHERE id=? FOR UPDATE",r.productId());
  if(found.isEmpty())throw Failure.missing();var p=found.get(0);
  if(((Number)p.get("seller_id")).longValue()!=r.expectedSellerId()||r.buyerId()==r.expectedSellerId())throw Failure.forbidden();
  if(!"ON_SALE".equals(p.get("status"))||((Number)p.get("quantity")).intValue()<r.quantity())throw Failure.conflict("商品下架或库存不足");
  if(!"LIST".equals(r.pricingMode())||((Number)p.get("version")).longValue()!=r.expectedProductVersion()||((Number)p.get("price_cent")).intValue()!=r.expectedListPriceCent())throw Failure.conflict("商品版本或价格已变化");
  String snapshot=encode(products.get(r.productId()));
  int remaining=((Number)p.get("quantity")).intValue()-r.quantity();
  db.update("UPDATE products SET quantity=?,status=?,off_shelf_reason=?,version=version+1,updated_at=NOW() WHERE id=?",remaining,remaining==0?"OFF_SALE":"ON_SALE",remaining==0?"SOLD_OUT":null,r.productId());
  db.update("UPDATE inventory_reservations SET status='RESERVED',product_snapshot=?,updated_at=NOW() WHERE operation_id=?",snapshot,r.operationId());
  return view(lock(r.operationId()));
 }
 @Transactional
 public Map<String,Object> confirm(String operation,long orderId){
  var r=lockOrMissing(operation);checkOrder(r,orderId);
  if("RELEASED".equals(r.get("status")))throw Failure.conflict("已经释放，不能确认");
  db.update("UPDATE inventory_reservations SET status='CONFIRMED',updated_at=NOW() WHERE operation_id=?",operation);
  return view(lock(operation));
 }
 @Transactional
 public Map<String,Object> release(String operation,long orderId){
  Hashes.key(operation);
  // 释放先到时保留终态墓碑，后到的 reserve 不能再次扣库存。
  db.update("INSERT INTO inventory_reservations(operation_id,order_id,quantity,status,created_at,updated_at) VALUES(?,?,0,'RELEASED',NOW(),NOW()) ON DUPLICATE KEY UPDATE operation_id=inventory_reservations.operation_id",operation,orderId);
  var r=lock(operation);checkOrder(r,orderId);
  if("RELEASED".equals(r.get("status")))return view(r);
  long product=((Number)r.get("product_id")).longValue();
  db.queryForMap("SELECT id FROM products WHERE id=? FOR UPDATE",product);
  db.update("UPDATE products SET quantity=quantity+?,status=IF(status='OFF_SALE' AND off_shelf_reason='SOLD_OUT','ON_SALE',status),off_shelf_reason=IF(off_shelf_reason='SOLD_OUT',NULL,off_shelf_reason),version=version+1,updated_at=NOW() WHERE id=?",r.get("quantity"),product);
  db.update("UPDATE inventory_reservations SET status='RELEASED',updated_at=NOW() WHERE operation_id=?",operation);
  return view(lock(operation));
 }
 private Map<String,Object> lockOrMissing(String operation){var rows=db.queryForList("SELECT * FROM inventory_reservations WHERE operation_id=? FOR UPDATE",operation);if(rows.isEmpty())throw Failure.missing();return rows.get(0);}
 private void checkOrder(Map<String,Object> row,long order){if(((Number)row.get("order_id")).longValue()!=order)throw Failure.conflict("订单绑定不一致");}
}

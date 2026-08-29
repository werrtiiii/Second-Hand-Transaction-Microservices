package com.secondhand.micro.trade;
import com.secondhand.micro.platform.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import java.util.*;
@Service public class PaymentLedger {
 private final JdbcTemplate db;private final boolean mock;
 public PaymentLedger(JdbcTemplate d,@Value("${app.mock-payments-enabled:false}") boolean m){db=d;mock=m;}
 public void requireMock(){if(!mock)throw new Failure(503,"PAYMENT_UNAVAILABLE","尚未配置真实支付渠道；模拟支付未启用");}
 @Transactional public Map<String,Object> create(long order,long buyer,String method){
  requireMock();var rows=db.queryForList("SELECT * FROM orders WHERE id=? AND buyer_id=? FOR UPDATE",order,buyer);if(rows.isEmpty())throw Failure.missing();var o=rows.get(0);
  if(!"WAIT_PAY".equals(o.get("status")))throw Failure.conflict("订单不可支付");
  db.update("INSERT INTO payments(payment_no,order_id,buyer_id,amount_cent,status,method,created_at,updated_at) VALUES(?,?,?,?,'WAIT_PAY',?,NOW(),NOW()) ON DUPLICATE KEY UPDATE order_id=payments.order_id","PAY-"+UUID.randomUUID(),order,buyer,o.get("amount_cent"),method);
  return db.queryForMap("SELECT * FROM payments WHERE order_id=?",order);
 }
 public Map<String,Object> owned(String no,long buyer){var rows=db.queryForList("SELECT * FROM payments WHERE payment_no=? AND buyer_id=?",no,buyer);if(rows.isEmpty())throw Failure.missing();return rows.get(0);}
 @Transactional public void pay(long order,long buyer){
  requireMock();create(order,buyer,"MOCK");
  db.update("UPDATE payments SET status='PAID',updated_at=NOW() WHERE order_id=? AND buyer_id=? AND status='WAIT_PAY'",order,buyer);
 }
 @Transactional public void refund(long afterSale,long order,int amount){
  var rows=db.queryForList("SELECT * FROM payments WHERE order_id=? FOR UPDATE",order);if(rows.isEmpty())throw Failure.conflict("没有可退款的支付记录");var p=rows.get(0);
  if(db.queryForObject("SELECT COUNT(*) FROM refunds WHERE after_sale_id=?",Long.class,afterSale)>0)return;
  int refunded=((Number)p.get("refunded_cent")).intValue();int paid=((Number)p.get("amount_cent")).intValue();
  if(amount<=0||amount>paid-refunded)throw Failure.conflict("退款超过可退金额");
  db.update("INSERT INTO refunds(refund_no,after_sale_id,order_id,amount_cent,created_at) VALUES(?,?,?,?,NOW())","REF-"+UUID.randomUUID(),afterSale,order,amount);
  db.update("UPDATE payments SET refunded_cent=?,status=?,updated_at=NOW() WHERE order_id=?",refunded+amount,refunded+amount==paid?"REFUNDED":"PAID",order);
 }
}

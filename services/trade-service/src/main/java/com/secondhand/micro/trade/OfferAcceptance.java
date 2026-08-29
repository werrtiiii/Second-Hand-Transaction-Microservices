package com.secondhand.micro.trade;
import com.secondhand.micro.platform.*;
import com.secondhand.order.entity.Order;
import com.secondhand.order.repository.OrderRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import java.util.*;
@Service public class OfferAcceptance {
 @org.springframework.beans.factory.annotation.Autowired private ProductClient products;
 private final JdbcTemplate db;private final TransactionTemplate tx;private final TradeStore saga;private final OrderRepository orders;
 public OfferAcceptance(JdbcTemplate d,PlatformTransactionManager m,TradeStore s,OrderRepository o){db=d;tx=new TransactionTemplate(m);saga=s;orders=o;}
 public Order accept(long seller,long offerId){
  var initial=db.queryForList("SELECT * FROM offers WHERE id=?",offerId);if(initial.isEmpty())throw Failure.missing();var current=initial.get(0);
  if(((Number)current.get("seller_id")).longValue()!=seller)throw Failure.forbidden();
  if("PENDING".equals(current.get("status"))){var product=products.getById(((Number)current.get("product_id")).longValue());if(!"ON_SALE".equals(product.status())||product.quantity()<1)throw Failure.conflict("商品已经售罄");}

  var offer=tx.execute(s->{var rows=db.queryForList("SELECT * FROM offers WHERE id=? FOR UPDATE",offerId);if(rows.isEmpty())throw Failure.missing();var o=rows.get(0);
   if(((Number)o.get("seller_id")).longValue()!=seller)throw Failure.forbidden();
   if(!Set.of("PENDING","ACCEPTING","ACCEPTED").contains(o.get("status")))throw Failure.conflict("报价不可接受");
   if("PENDING".equals(o.get("status")))db.update("UPDATE offers SET status='ACCEPTING',version=version+1,updated_at=NOW() WHERE id=?",offerId);
   return o;
  });
  if(offer.get("order_id")!=null)return orders.findById(((Number)offer.get("order_id")).longValue()).orElseThrow();
  var result=saga.create(((Number)offer.get("buyer_id")).longValue(),"offer-accept-"+offerId,new TradeController.Create(((Number)offer.get("product_id")).longValue(),null,null,null,null,offerId));
  long order=((Number)result.get("id")).longValue();
  if("CREATE_FAILED".equals(result.get("status"))){db.update("UPDATE offers SET status='REJECTED',version=version+1 WHERE id=? AND status='ACCEPTING'",offerId);throw Failure.conflict("库存预占失败");}
  if("CREATING".equals(result.get("status")))throw new Failure(503,"PENDING","报价接受正在恢复，请使用同一报价重试");
  db.update("UPDATE offers SET status='ACCEPTED',order_id=?,version=version+1,updated_at=NOW() WHERE id=? AND status='ACCEPTING'",order,offerId);
  return orders.findById(order).orElseThrow();
 }
 @Scheduled(fixedDelayString="${app.recovery-delay-ms:5000}") public void recover(){
  for(var offer:db.queryForList("SELECT id,seller_id FROM offers WHERE status='ACCEPTING' LIMIT 50")){
   try{accept(((Number)offer.get("seller_id")).longValue(),((Number)offer.get("id")).longValue());}catch(Exception e){org.slf4j.LoggerFactory.getLogger(getClass()).warn("报价恢复等待重试 offerId={}",offer.get("id"));}
  }
 }
}

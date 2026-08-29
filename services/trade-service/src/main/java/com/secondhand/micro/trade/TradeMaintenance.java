package com.secondhand.micro.trade;
import com.secondhand.order.service.OrderService;
import com.secondhand.aftersale.service.AfterSaleService;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
/** 后台任务只推进本域状态；版本锁使多副本的重复执行不能覆盖新状态。 */
@Component public class TradeMaintenance {
 private final OrderService orders;private final AfterSaleService afterSales;
 public TradeMaintenance(OrderService o,AfterSaleService a){orders=o;afterSales=a;}
 @Scheduled(initialDelayString="${app.maintenance-delay-ms:60000}",fixedDelayString="${app.maintenance-delay-ms:60000}") public void maintain(){
  afterSales.processTimeouts();orders.processSettlements();
 }
}

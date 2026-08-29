package com.secondhand.payment;
import com.secondhand.micro.trade.PaymentLedger;
import com.secondhand.micro.platform.Failure;
import org.springframework.stereotype.Service;
import java.util.Map;
/** 兼容模拟渠道契约；状态改为数据库持久化，不接受未经验证的外部回调。 */
@Service public class MockPaymentService implements PaymentService {
 private final PaymentLedger ledger;public MockPaymentService(PaymentLedger l){ledger=l;}
 public PaymentResult createPayment(CreatePaymentCommand c){var p=ledger.create(c.orderId(),c.userId(),c.method().name());return new PaymentResult((String)p.get("payment_no"),null,null,PaymentStatus.valueOf((String)p.get("status")));}
 public PaymentStatus queryPayment(String no){throw new Failure(403,"FORBIDDEN","必须校验付款人身份");}
 public void handleCallback(String platform,Map<String,String> params){throw new Failure(403,"FORBIDDEN","未配置回调验签渠道");}
 public RefundResult refund(String no,Integer amount,String reason){throw new Failure(403,"FORBIDDEN","退款必须关联授权的售后单");}
}

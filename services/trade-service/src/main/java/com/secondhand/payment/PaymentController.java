package com.secondhand.payment;
import com.secondhand.auth.security.AuthPrincipal;
import com.secondhand.common.ApiResponse;
import com.secondhand.micro.trade.PaymentLedger;
import com.secondhand.micro.platform.Failure;
import com.secondhand.order.service.OrderService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/payments") public class PaymentController {
 private final PaymentLedger ledger;private final PaymentService payments;private final OrderService orders;
 public PaymentController(PaymentLedger l,PaymentService p,OrderService o){ledger=l;payments=p;orders=o;}
 @PostMapping public ApiResponse<?> create(@AuthenticationPrincipal AuthPrincipal p,@RequestBody CreatePaymentRequest r){var row=ledger.create(r.orderId(),p.userId(),r.method()==null?"MOCK":r.method().name());return ApiResponse.ok(new PaymentService.PaymentResult((String)row.get("payment_no"),null,null,PaymentService.PaymentStatus.valueOf((String)row.get("status"))));}
 @GetMapping("/{paymentNo}") public ApiResponse<?> query(@AuthenticationPrincipal AuthPrincipal p,@PathVariable String paymentNo){return ApiResponse.ok(ledger.owned(paymentNo,p.userId()).get("status"));}
 @PostMapping("/{paymentNo}/mock-pay") public ApiResponse<?> pay(@AuthenticationPrincipal AuthPrincipal p,@PathVariable String paymentNo,@RequestParam long orderId){ledger.requireMock();var payment=ledger.owned(paymentNo,p.userId());if(((Number)payment.get("order_id")).longValue()!=orderId)throw Failure.conflict("支付单与订单不匹配");if("WAIT_PAY".equals(payment.get("status")))orders.pay(p.userId(),orderId);else if(!"PAID".equals(payment.get("status")))throw Failure.conflict("支付单已关闭");return ApiResponse.ok("支付成功");}
 record CreatePaymentRequest(long orderId,PaymentService.PaymentMethod method){}
}

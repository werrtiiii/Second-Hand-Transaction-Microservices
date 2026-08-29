package com.secondhand.micro.trade;
import com.secondhand.micro.platform.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController public class TradeController {
 private final TradeStore trades;private final IdentityClient identity;
 public TradeController(TradeStore t,IdentityClient i){trades=t;identity=i;}
 public record Create(@Positive long productId,@Size(max=255) String receiverName,@Size(max=255) String receiverPhone,@Size(max=255) String receiverAddress,@Positive Long addressId,@Null Long offerId){
  public Create(long p,String n,String phone,String a){this(p,n,phone,a,null,null);}
  @AssertTrue(message="请选择本人地址或填写完整收货信息") public boolean isReceiverComplete(){return addressId!=null||(receiverName!=null&&!receiverName.isBlank()&&receiverPhone!=null&&!receiverPhone.isBlank()&&receiverAddress!=null&&!receiverAddress.isBlank());}
 }
 @PostMapping("/api/orders") public ResponseEntity<?> create(@RequestHeader(value="Authorization",required=false) String auth,@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody Create r){
  var result=trades.create(identity.requireUser(auth),key,r);int status="CREATING".equals(result.get("status"))?202:"CREATE_FAILED".equals(result.get("status"))?409:200;
  return ResponseEntity.status(status).body(status==409?new Api<>(false,result,new Api.Error("CONFLICT","订单创建失败")):Api.ok(result));
 }
 @PostMapping("/api/orders/{id}/cancel") public Api<?> cancel(@RequestHeader(value="Authorization",required=false) String auth,@PathVariable long id){return Api.ok(trades.cancel(identity.requireUser(auth),id));}
 @GetMapping("/internal/v1/orders/{orderId}/inventory-state") public Api<?> state(HttpServletRequest req,@PathVariable long orderId){InternalGuard.require(req,"product-service");return Api.ok(trades.inventoryState(orderId));}
}

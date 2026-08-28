package com.secondhand.micro.trade;
import com.secondhand.micro.platform.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
public class TradeController {
 private final TradeStore trades; private final IdentityClient identity;
 public TradeController(TradeStore trades,IdentityClient identity){this.trades=trades;this.identity=identity;}
 public record Create(@Positive long productId,@NotBlank @Size(max=255) String receiverName,@NotBlank @Size(max=255) String receiverPhone,@NotBlank @Size(max=255) String receiverAddress){}
 @PostMapping("/api/orders") ResponseEntity<?> create(@RequestHeader(value="Authorization",required=false) String auth,@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody Create r){
  var result=trades.create(identity.requireUser(auth),key,r);
  int status="CREATING".equals(result.get("status"))?202:"CREATE_FAILED".equals(result.get("status"))?409:200;
  return ResponseEntity.status(status).body(status==409?new Api<>(false,result,new Api.Error("CONFLICT","订单创建失败，请检查商品状态")):Api.ok(result));
 }
 @GetMapping("/api/orders/{id}") Api<?> get(@RequestHeader(value="Authorization",required=false) String auth,@PathVariable long id){return Api.ok(trades.get(identity.requireUser(auth),id));}
 @PostMapping("/api/orders/{id}/cancel") Api<?> cancel(@RequestHeader(value="Authorization",required=false) String auth,@PathVariable long id){return Api.ok(trades.cancel(identity.requireUser(auth),id));}
 @GetMapping("/internal/v1/orders/{orderId}/inventory-state") Api<?> state(HttpServletRequest req,@PathVariable long orderId){InternalGuard.require(req,"product-service");return Api.ok(trades.inventoryState(orderId));}
}

package com.secondhand.micro.product;
import com.secondhand.micro.platform.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/internal/v1/inventory/reservations")
public class InventoryController {
 private final InventoryStore store;
 public InventoryController(InventoryStore store){this.store=store;}
 public record Reserve(@NotBlank @Pattern(regexp="[A-Za-z0-9:_-]{1,80}") String operationId,@Positive long orderId,@Positive long productId,@Positive long buyerId,@Positive long expectedSellerId,@Min(1) @Max(1) int quantity,@Min(0) long expectedProductVersion,@NotBlank String pricingMode,@Min(1) int expectedListPriceCent){}
 public record Binding(@Positive long orderId){}
 @PostMapping Api<?> reserve(HttpServletRequest req,@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody Reserve r){InternalGuard.require(req,"trade-service");return Api.ok(store.reserve(r,key));}
 @GetMapping("/{operation}") Api<?> get(HttpServletRequest req,@PathVariable String operation){InternalGuard.require(req,"trade-service");return Api.ok(store.get(operation));}
 @PostMapping("/{operation}/confirm") Api<?> confirm(HttpServletRequest req,@PathVariable String operation,@Valid @RequestBody Binding r){InternalGuard.require(req,"trade-service");return Api.ok(store.confirm(operation,r.orderId()));}
 @PostMapping("/{operation}/release") Api<?> release(HttpServletRequest req,@PathVariable String operation,@Valid @RequestBody Binding r){InternalGuard.require(req,"trade-service");return Api.ok(store.release(operation,r.orderId()));}
}

package com.secondhand.micro.trade;
import com.secondhand.order.service.OrderService;
import com.secondhand.common.ApiResponse;
import org.springframework.web.bind.annotation.*;
@RestController public class SellerSales {
 private final OrderService orders;public SellerSales(OrderService o){orders=o;}
 @GetMapping("/api/users/{sellerId}/sold") public ApiResponse<?> list(@PathVariable long sellerId){return ApiResponse.ok(orders.getSellerSoldProducts(sellerId));}
}

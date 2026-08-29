package com.secondhand.micro.product;
import com.secondhand.product.service.ProductService;
import com.secondhand.common.ApiResponse;
import org.springframework.web.bind.annotation.*;
@RestController public class SellerProducts {
 private final ProductService products;public SellerProducts(ProductService p){products=p;}
 @GetMapping("/api/users/{sellerId}/products") public ApiResponse<?> list(@PathVariable long sellerId,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="10") int size){return ApiResponse.ok(products.getSellerOnSaleProducts(sellerId,page,Math.min(50,size)));}
}

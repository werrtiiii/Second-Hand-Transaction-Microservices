package com.secondhand.micro.product;
import com.secondhand.micro.platform.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
@RestController
public class ProductController {
 private final ProductStore products; private final IdentityClient identity;
 public ProductController(ProductStore p,IdentityClient i){products=p;identity=i;}
 public record Create(@NotBlank @Size(max=100) String title,@NotNull @Min(1) Integer priceCent,@Size(max=512) String coverImageUrl,@NotBlank String description,@Min(1) Integer quantity,Boolean freeShipping,@Min(0) Integer shippingFeeCent){}


 @GetMapping("/internal/v1/products/{productId}") Api<?> internal(HttpServletRequest req,@PathVariable long productId){InternalGuard.require(req,"user-service","trade-service");return Api.ok(products.get(productId));}
}

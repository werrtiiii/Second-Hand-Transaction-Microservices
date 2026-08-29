package com.secondhand.micro.product;
import com.secondhand.micro.platform.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
@RestController public class InternalProductQueries {
 private final JdbcTemplate db;public InternalProductQueries(JdbcTemplate d){db=d;}
 @GetMapping("/internal/v1/products/stats") public Api<?> stats(HttpServletRequest r){InternalGuard.require(r,"user-service","trade-service");return Api.ok(Map.of("totalProducts",db.queryForObject("SELECT COUNT(*) FROM products",Long.class),"onSaleProducts",db.queryForObject("SELECT COUNT(*) FROM products WHERE status='ON_SALE'",Long.class)));}
}

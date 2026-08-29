package com.secondhand.micro.trade;
import com.secondhand.micro.platform.*;
import com.secondhand.rating.service.RatingService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
@RestController public class InternalTradeQueries {
 private final JdbcTemplate db;private final RatingService ratings;
 public InternalTradeQueries(JdbcTemplate d,RatingService r){db=d;ratings=r;}
 @GetMapping("/internal/v1/trade/stats") public Api<?> stats(HttpServletRequest r){InternalGuard.require(r,"user-service");return Api.ok(Map.of("totalOrders",db.queryForObject("SELECT COUNT(*) FROM orders",Long.class),"todayNewOrders",db.queryForObject("SELECT COUNT(*) FROM orders WHERE created_at>=CURRENT_DATE",Long.class),"orderStatusDistribution",db.queryForList("SELECT status,COUNT(*) AS count FROM orders GROUP BY status")));}
 @GetMapping("/internal/v1/ratings/{sellerId}") public Api<?> rating(HttpServletRequest r,@PathVariable long sellerId){InternalGuard.require(r,"user-service");return Api.ok(ratings.getSellerRating(sellerId));}
 @GetMapping("/internal/v1/users/{userId}/trade-counts") public Api<?> counts(HttpServletRequest r,@PathVariable long userId){InternalGuard.require(r,"user-service");return Api.ok(Map.of("pendingOffersReceived",db.queryForObject("SELECT COUNT(*) FROM offers WHERE seller_id=? AND status='PENDING'",Long.class,userId),"pendingOrdersBuyer",db.queryForObject("SELECT COUNT(*) FROM orders WHERE buyer_id=? AND status IN ('WAIT_PAY','WAIT_RECEIVE')",Long.class,userId),"pendingOrdersSeller",db.queryForObject("SELECT COUNT(*) FROM orders WHERE seller_id=? AND status='WAIT_DELIVER'",Long.class,userId)));}
}

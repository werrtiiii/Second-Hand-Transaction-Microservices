package com.secondhand.user.controller;

import com.secondhand.auth.security.AuthPrincipal;
import com.secondhand.chat.repository.ChatMessageRepository;
import com.secondhand.common.ApiResponse;
import com.secondhand.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final ChatMessageRepository chatMessageRepo;

    private final com.secondhand.micro.user.TradeQueries trade;
    public UserController(UserService u,ChatMessageRepository c,com.secondhand.micro.user.TradeQueries t){userService=u;chatMessageRepo=c;trade=t;}

    /** 获取卖家公开信息 */
    @GetMapping("/{id}/public")
    public ApiResponse<UserService.PublicUserDto> publicInfo(@PathVariable("id") Long userId) {
        return ApiResponse.ok(userService.getPublicInfo(userId));
    }



    /** 获取个人资料 */
    @GetMapping("/profile")
    public ApiResponse<UserService.ProfileDto> profile(
            @AuthenticationPrincipal AuthPrincipal principal) {
        return ApiResponse.ok(userService.getProfile(principal.userId()));
    }

    /** 更新个人资料 */
    @PutMapping("/profile")
    public ApiResponse<UserService.ProfileDto> updateProfile(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest req) {
        return ApiResponse.ok(userService.updateProfile(principal.userId(),
                req.nickname(), req.phone(), req.email()));
    }

    /** 上传头像 */
    @PutMapping("/avatar")
    public ApiResponse<String> uploadAvatar(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam("file") MultipartFile file) {
        String url = userService.uploadAvatar(principal.userId(), file);
        return ApiResponse.ok(url);
    }

    /** 通知聚合：未读消息、待处理报价、待处理订单 */
    @GetMapping("/notifications")
    public ApiResponse<NotificationCounts> notifications(
            @AuthenticationPrincipal AuthPrincipal principal) {
        long userId = principal.userId();

        long unreadMessages = chatMessageRepo.countByReceiverIdAndIsReadFalse(userId);
        var counts=trade.counts(userId);
        long pendingOffersReceived=counts.path("pendingOffersReceived").asLong();
        long pendingOrdersBuyer=counts.path("pendingOrdersBuyer").asLong();
        long pendingOrdersSeller=counts.path("pendingOrdersSeller").asLong();

        return ApiResponse.ok(new NotificationCounts(
                unreadMessages, pendingOffersReceived, pendingOrdersBuyer, pendingOrdersSeller));
    }

    record UpdateProfileRequest(
            @Size(max = 50) String nickname,
            @Size(max = 20) String phone,
            @Size(max = 128) String email) {}

    record NotificationCounts(long unreadMessages, long pendingOffersReceived,
                              long pendingOrdersBuyer, long pendingOrdersSeller) {}
}

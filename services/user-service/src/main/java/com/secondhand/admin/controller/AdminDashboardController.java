package com.secondhand.admin.controller;

import com.secondhand.admin.service.AdminService;
import com.secondhand.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name="app.service-name",havingValue="user-service")
public class AdminDashboardController {

    private final AdminService adminService;

    public AdminDashboardController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<java.util.Map<String,Object>> dashboard() {
        return ApiResponse.ok(adminService.getDashboard());
    }
}

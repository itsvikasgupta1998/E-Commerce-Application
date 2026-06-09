package com.app.controllers;

import com.app.payloads.UserResponse;
import com.app.services.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/assign/{userId}")
    public UserResponse makeAdmin(@PathVariable Long userId) {
        return adminService.assignAdminRole(userId);
    }
}
package com.app.services;

import com.app.payloads.UserResponse;

public interface AdminService {
    UserResponse assignAdminRole(Long userId);
}

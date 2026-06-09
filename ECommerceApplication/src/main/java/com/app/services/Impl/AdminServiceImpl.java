package com.app.services.Impl;

import com.app.entites.Role;
import com.app.entites.User;
import com.app.enums.RoleType;
import com.app.exceptions.ResourceNotFoundException;
import com.app.mappers.UserMapper;
import com.app.payloads.UserResponse;
import com.app.repositories.RoleRepository;
import com.app.repositories.UserRepository;
import com.app.services.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse assignAdminRole(Long userId) {

        log.info("Admin role assignment started for userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "userId", userId
                ));

        Role adminRole = roleRepository.findByRoleType(RoleType.ROLE_ADMIN)
                .orElseThrow(() -> {
                    log.error("ROLE_ADMIN not found in DB");
                    return new RuntimeException("ROLE_ADMIN missing");
                });

        if (user.getRoles().contains(adminRole)) {
            log.warn("User already has ADMIN role. userId={}", userId);
            return userMapper.toResponse(user);
        }

        user.getRoles().add(adminRole);

        User updated = userRepository.save(user);

        log.info("ADMIN role assigned successfully. userId={}", userId);

        return userMapper.toResponse(updated);
    }
}
package com.app.config;

import com.app.entites.Role;
import com.app.enums.RoleType;
import com.app.entites.User;
import com.app.repositories.RoleRepository;
import com.app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // 1. Create roles if not exist
        createRoleIfNotFound(RoleType.ROLE_USER);
        createRoleIfNotFound(RoleType.ROLE_ADMIN);

        // 2. Create default admin if not exists
        if (!userRepository.existsByEmail("admin@system.com")) {

            Role adminRole = roleRepository.findByRoleType(RoleType.ROLE_ADMIN)
                    .orElseThrow();

            User admin = new User();
            admin.setFirstName("System");
            admin.setLastName("Admin");
            admin.setEmail("admin@system.com");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setRoles(Set.of(adminRole));
            admin.setEnabled(true);

            userRepository.save(admin);
        }
    }

    private void createRoleIfNotFound(RoleType roleType) {

        roleRepository.findByRoleType(roleType)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleType(roleType);
                    return roleRepository.save(role);
                });
    }
}
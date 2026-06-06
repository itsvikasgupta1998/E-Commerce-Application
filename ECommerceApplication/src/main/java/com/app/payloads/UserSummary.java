package com.app.payloads;

import lombok.*;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummary {

    private Long userId;

    private String firstName;

    private String lastName;

    private String email;

    private Set<String> roles;

    private Boolean emailVerified;
}

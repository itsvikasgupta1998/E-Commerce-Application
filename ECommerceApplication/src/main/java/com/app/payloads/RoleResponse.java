package com.app.payloads;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {

    private Long roleId;

    private String roleName;
}

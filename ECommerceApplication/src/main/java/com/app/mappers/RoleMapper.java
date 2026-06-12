package com.app.mappers;

import com.app.entites.Role;
import com.app.payloads.RoleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(
            target = "roleName",
            expression = "java(role.getRoleType().name())"
    )
    RoleResponse toResponse(Role role);
}

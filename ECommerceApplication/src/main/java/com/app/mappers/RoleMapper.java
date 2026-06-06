package com.app.mappers;

import com.app.entites.Role;
import com.app.payloads.RoleResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleResponse toResponse(Role role);
}

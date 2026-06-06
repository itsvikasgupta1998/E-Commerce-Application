package com.app.mappers;

import com.app.entites.User;
import com.app.payloads.UserRegistrationRequest;
import com.app.payloads.UserResponse;
import com.app.payloads.UserUpdateRequest;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        uses = {
                AddressMapper.class,
                RoleMapper.class,
                CartMapper.class
        }
)
public interface UserMapper {

    User toEntity(UserRegistrationRequest request);

    UserResponse toResponse(User user);

    @BeanMapping(
            nullValuePropertyMappingStrategy =
                    NullValuePropertyMappingStrategy.IGNORE
    )
    void updateUserFromRequest(
            UserUpdateRequest request,
            @MappingTarget User user
    );
}
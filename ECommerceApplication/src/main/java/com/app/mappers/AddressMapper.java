package com.app.mappers;

import com.app.entites.Address;
import com.app.payloads.AddressRequest;
import com.app.payloads.AddressResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    Address toEntity(
            AddressRequest request
    );

    AddressResponse toResponse(
            Address address
    );

    @BeanMapping(
            nullValuePropertyMappingStrategy =
                    NullValuePropertyMappingStrategy.IGNORE
    )
    void updateEntity(
            AddressRequest request,
            @MappingTarget Address address
    );
}
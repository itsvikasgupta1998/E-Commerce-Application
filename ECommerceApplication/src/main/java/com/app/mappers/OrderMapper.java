package com.app.mappers;

import com.app.entites.Order;
import com.app.payloads.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = {
                OrderItemMapper.class,
                PaymentMapper.class
        }
)
public interface OrderMapper {

    @Mapping(
            target = "email",
            source = "user.email"
    )
    OrderResponse toResponse(Order order);
}
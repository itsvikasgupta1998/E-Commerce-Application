package com.app.mappers;

import com.app.entites.Order;
import com.app.payloads.OrderResponse;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring",
        uses = {
                OrderItemMapper.class,
                PaymentMapper.class
        }
)
public interface OrderMapper {

    OrderResponse toResponse(Order order);
}
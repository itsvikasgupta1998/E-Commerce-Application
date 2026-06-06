package com.app.mappers;

import com.app.entites.OrderItem;
import com.app.payloads.OrderItemResponse;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring",
        uses = ProductMapper.class
)
public interface OrderItemMapper {

    OrderItemResponse toResponse(OrderItem orderItem);
}

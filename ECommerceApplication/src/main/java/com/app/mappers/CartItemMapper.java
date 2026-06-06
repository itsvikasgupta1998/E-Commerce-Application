package com.app.mappers;

import com.app.entites.CartItem;
import com.app.payloads.CartItemResponse;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring",
        uses = ProductMapper.class
)
public interface CartItemMapper {

    CartItemResponse toResponse(CartItem cartItem);
}

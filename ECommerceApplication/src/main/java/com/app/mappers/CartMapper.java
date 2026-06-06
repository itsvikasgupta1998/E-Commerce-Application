package com.app.mappers;

import com.app.entites.Cart;
import com.app.payloads.CartResponse;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring",
        uses = CartItemMapper.class
)
public interface CartMapper {

    CartResponse toResponse(Cart cart);
}

package com.app.mappers;

import com.app.entites.Cart;
import com.app.payloads.CartResponse;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        uses = CartItemMapper.class
)
public interface CartMapper {

    @Mapping(target = "items", source = "cartItems")
    @Mapping(
            target = "totalItems",
            expression = """
            java(
                cart.getCartItems() == null
                ? 0
                : cart.getCartItems()
                      .stream()
                      .mapToInt(item -> item.getQuantity())
                      .sum()
            )
            """
    )
    CartResponse toResponse(Cart cart);
}
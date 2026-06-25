package com.app.mappers;

import com.app.entites.CartItem;
import com.app.payloads.CartItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.math.BigDecimal;


@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(
            target = "productId",
            source = "product.productId"
    )
    @Mapping(
            target = "productName",
            source = "product.productName"
    )
    @Mapping(
            target = "unitPrice",
            source = "productPrice"
    )
    @Mapping(
            target = "lineTotal",
            expression =
                    "java(calculateLineTotal(cartItem))"
    )
    CartItemResponse toResponse(
            CartItem cartItem
    );

    default BigDecimal calculateLineTotal(
            CartItem cartItem
    ) {

        if (cartItem.getProductPrice() == null
                || cartItem.getQuantity() == null) {
            return BigDecimal.ZERO;
        }

        return cartItem.getProductPrice()
                .multiply(
                        BigDecimal.valueOf(
                                cartItem.getQuantity()
                        )
                );
    }
}
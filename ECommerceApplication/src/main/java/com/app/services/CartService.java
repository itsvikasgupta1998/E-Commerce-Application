package com.app.services;

import com.app.payloads.CartResponse;
import java.util.List;



public interface CartService {

	CartResponse addProductToCart(
			Long cartId,
			Long productId,
			Integer quantity
	);

	CartResponse getCart(
			String email,
			Long cartId
	);

	List<CartResponse> getAllCarts();

	CartResponse updateCartItemQuantity(
			Long cartId,
			Long productId,
			Integer quantity
	);

	void removeProductFromCart(
			Long cartId,
			Long productId
	);

	void refreshProductPriceInCart(
			Long cartId,
			Long productId
	);
}
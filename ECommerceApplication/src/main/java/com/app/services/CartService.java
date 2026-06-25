package com.app.services;

import com.app.payloads.CartResponse;


public interface CartService {

	CartResponse getCurrentUserCart();

	CartResponse addProductToCart(
			Long productId,
			Integer quantity
	);

	CartResponse updateCartItemQuantity(
			Long productId,
			Integer quantity
	);

	void removeProductFromCart(
			Long productId
	);

	void refreshProductPriceInCart(Long productId);
}
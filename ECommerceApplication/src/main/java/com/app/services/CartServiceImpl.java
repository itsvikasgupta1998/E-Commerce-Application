package com.app.services;

import java.math.BigDecimal;
import java.util.List;
import com.app.mappers.CartMapper;
import com.app.payloads.CartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.app.entites.Cart;
import com.app.entites.CartItem;
import com.app.entites.Product;
import com.app.exceptions.ResourceNotFoundException;
import com.app.repositories.CartItemRepository;
import com.app.repositories.CartRepository;
import com.app.repositories.ProductRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

	private final CartRepository cartRepository;
	private final ProductRepository productRepository;
	private final CartItemRepository cartItemRepository;
	private final CartMapper cartMapper;

	private Cart getCartEntity(Long cartId) {

		return cartRepository.findById(cartId)
				.orElseThrow(() ->
						new ResourceNotFoundException(
								"Cart",
								"cartId",
								cartId
						));
	}

	private Product getProductEntity(Long productId) {

		return productRepository.findById(productId)
				.orElseThrow(() ->
						new ResourceNotFoundException(
								"Product",
								"productId",
								productId
						));
	}

	private void recalculateCart(Cart cart) {

		BigDecimal totalPrice =
				cart.getCartItems()
						.stream()
						.map(item ->
								item.getProductPrice()
										.multiply(
												BigDecimal.valueOf(
														item.getQuantity()
												)
										)
						)
						.reduce(
								BigDecimal.ZERO,
								BigDecimal::add
						);

		cart.setTotalPrice(totalPrice);
	}


	@Override
	public CartResponse addProductToCart(
			Long cartId,
			Long productId,
			Integer quantity
	) {

		Cart cart = getCartEntity(cartId);

		Product product = getProductEntity(productId);

		if (quantity <= 0) {
			throw new IllegalArgumentException(
					"Quantity must be greater than zero"
			);
		}

		if (product.getQuantity() < quantity) {
			throw new IllegalStateException(
					"Insufficient stock available"
			);
		}

		CartItem cartItem =
				cartItemRepository
						.findCartItemByProductIdAndCartId(
								cartId,
								productId
						);

		if (cartItem != null) {

			cartItem.setQuantity(
					cartItem.getQuantity() + quantity
			);

		} else {

			cartItem = new CartItem();

			cartItem.setCart(cart);
			cartItem.setProduct(product);

			cartItem.setQuantity(quantity);

			cartItem.setDiscount(
					product.getDiscount()
			);

			cartItem.setProductPrice(
					product.getSpecialPrice()
			);

			cart.getCartItems().add(cartItem);
		}

		recalculateCart(cart);

		cartRepository.save(cart);

		return cartMapper.toResponse(cart);
	}


	@Override
	@Transactional(readOnly = true)
	public CartResponse getCart(
			String email,
			Long cartId
	) {

		Cart cart =
				cartRepository
						.findCartByUserEmailAndCartId(
								email,
								cartId
						)
						.orElseThrow(() ->
								new ResourceNotFoundException(
										"Cart",
										"cartId",
										cartId
								)
						);

		return cartMapper.toResponse(cart);
	}

	@Override
	@Transactional(readOnly = true)
	public List<CartResponse> getAllCarts() {

		return cartRepository.findAll()
				.stream()
				.map(cartMapper::toResponse)
				.toList();
	}

	@Override
	public CartResponse updateCartItemQuantity(
			Long cartId,
			Long productId,
			Integer quantity
	) {

		Cart cart = getCartEntity(cartId);

		CartItem cartItem =
				cartItemRepository
						.findCartItemByProductIdAndCartId(
								cartId,
								productId
						);

		if (cartItem == null) {

			throw new ResourceNotFoundException(
					"CartItem",
					"productId",
					productId
			);
		}

		cartItem.setQuantity(quantity);

		recalculateCart(cart);

		cartRepository.save(cart);

		return cartMapper.toResponse(cart);
	}

	@Override
	public void removeProductFromCart(
			Long cartId,
			Long productId
	) {

		Cart cart = getCartEntity(cartId);

		CartItem cartItem =
				cartItemRepository
						.findCartItemByProductIdAndCartId(
								cartId,
								productId
						);

		if (cartItem == null) {

			throw new ResourceNotFoundException(
					"CartItem",
					"productId",
					productId
			);
		}

		cart.getCartItems().remove(cartItem);

		cartItemRepository.delete(cartItem);

		recalculateCart(cart);

		cartRepository.save(cart);
	}

	@Override
	public void refreshProductPriceInCart(
			Long cartId,
			Long productId
	) {

		CartItem cartItem =
				cartItemRepository
						.findCartItemByProductIdAndCartId(
								cartId,
								productId
						);

		if (cartItem == null) {
			return;
		}

		Product product =
				getProductEntity(productId);

		cartItem.setProductPrice(
				product.getSpecialPrice()
		);

		cartItem.setDiscount(
				product.getDiscount()
		);

		cartItemRepository.save(cartItem);

		recalculateCart(
				cartItem.getCart()
		);
	}
}








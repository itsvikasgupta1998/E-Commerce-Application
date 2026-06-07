package com.app.services.Impl;

import java.math.BigDecimal;
import java.util.List;
import com.app.exceptions.APIException;
import com.app.mappers.CartMapper;
import com.app.payloads.CartResponse;
import com.app.services.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.app.entites.Cart;
import com.app.entites.CartItem;
import com.app.entites.Product;
import com.app.exceptions.ResourceNotFoundException;
import com.app.repositories.CartItemRepository;
import com.app.repositories.CartRepository;
import com.app.repositories.ProductRepository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
				.orElseThrow(() -> {

					log.warn(
							"Cart not found. cartId={}",
							cartId
					);

					return new ResourceNotFoundException(
							"Cart",
							"cartId",
							cartId
					);
				});
	}

	private Product getProductEntity(Long productId) {

		return productRepository.findById(productId)
				.orElseThrow(() -> {

					log.warn(
							"Product not found. productId={}",
							productId
					);

					return new ResourceNotFoundException(
							"Product",
							"productId",
							productId
					);
				});
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

		log.debug(
				"Cart recalculated. cartId={}, totalPrice={}",
				cart.getCartId(),
				totalPrice
		);
	}


	@Override
	public CartResponse addProductToCart(
			Long cartId,
			Long productId,
			Integer quantity
	) {
		log.info(
				"Add product to cart request. cartId={}, productId={}, quantity={}",
				cartId,
				productId,
				quantity
		);

		Cart cart = getCartEntity(cartId);

		Product product = getProductEntity(productId);

		if (quantity <= 0) {

			log.warn(
					"Invalid quantity received. cartId={}, productId={}, quantity={}",
					cartId,
					productId,
					quantity
			);

			throw new APIException(
					"Quantity must be greater than zero"
			);
		}

		if (product.getQuantity() < quantity) {

			log.warn(
					"Insufficient stock. productId={}, available={}, requested={}",
					productId,
					product.getQuantity(),
					quantity
			);

			throw new APIException(
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
			if (quantity <= 0) {

				log.warn(
						"Invalid cart quantity update. cartId={}, productId={}, quantity={}",
						cartId,
						productId,
						quantity
				);

				throw new APIException(
						"Quantity must be greater than zero"
				);
			}
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
		log.info(
				"Product added to cart successfully. cartId={}, productId={}, quantity={}",
				cartId,
				productId,
				quantity
		);
		return cartMapper.toResponse(cart);
	}


	@Override
	@Transactional(readOnly = true)
	public CartResponse getCart(
			String email,
			Long cartId
	) {

		log.debug(
				"Fetching cart. cartId={}, email={}",
				cartId,
				email
		);

		Cart cart =
				cartRepository
						.findCartByUserEmailAndCartId(
								email,
								cartId
						)
						.orElseThrow(() -> {

							log.warn(
									"Cart not found. cartId={}, email={}",
									cartId,
									email
							);

							return new ResourceNotFoundException(
									"Cart",
									"cartId",
									cartId
							);
						});

		log.debug(
				"Cart fetched successfully. cartId={}",
				cartId
		);
		return cartMapper.toResponse(cart);

	}

	@Override
	@Transactional(readOnly = true)
	public List<CartResponse> getAllCarts() {
		log.debug("Fetching all carts");


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

		log.info(
				"Updating cart item quantity. cartId={}, productId={}, quantity={}",
				cartId,
				productId,
				quantity
		);

		Cart cart = getCartEntity(cartId);

		CartItem cartItem =
				cartItemRepository
						.findCartItemByProductIdAndCartId(
								cartId,
								productId
						);

		if (cartItem == null) {

			log.warn(
					"Cart item not found. cartId={}, productId={}",
					cartId,
					productId
			);
			throw new ResourceNotFoundException(
					"CartItem",
					"productId",
					productId
			);
		}

		cartItem.setQuantity(quantity);

		recalculateCart(cart);

		cartRepository.save(cart);

		log.info(
				"Cart item quantity updated. cartId={}, productId={}, quantity={}",
				cartId,
				productId,
				quantity
		);
		return cartMapper.toResponse(cart);
	}

	@Override
	public void removeProductFromCart(
			Long cartId,
			Long productId
	) {

		log.info(
				"Removing product from cart. cartId={}, productId={}",
				cartId,
				productId
		);

		Cart cart = getCartEntity(cartId);

		CartItem cartItem =
				cartItemRepository
						.findCartItemByProductIdAndCartId(
								cartId,
								productId
						);

		if (cartItem == null) {

			log.warn(
					"Cart item not found for removal. cartId={}, productId={}",
					cartId,
					productId
			);

			throw new ResourceNotFoundException(
					"CartItem",
					"productId",
					productId
			);
		}

		cart.getCartItems().remove(cartItem);

		cartItemRepository.delete(cartItem);

		recalculateCart(cart);
		log.info(
				"Product removed from cart successfully. cartId={}, productId={}",
				cartId,
				productId
		);
		cartRepository.save(cart);
	}

	@Override
	public void refreshProductPriceInCart(
			Long cartId,
			Long productId
	) {

		log.debug(
				"Refreshing cart item price. cartId={}, productId={}",
				cartId,
				productId
		);

		CartItem cartItem =
				cartItemRepository
						.findCartItemByProductIdAndCartId(
								cartId,
								productId
						);

		if (cartItem == null) {
			log.debug(
					"Cart item not found while refreshing price. cartId={}, productId={}",
					cartId,
					productId
			);
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
		log.debug(
				"Cart item price refreshed successfully. cartId={}, productId={}",
				cartId,
				productId
		);
		recalculateCart(
				cartItem.getCart()
		);
		cartRepository.save(cartItem.getCart());
	}
}








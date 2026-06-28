package com.app.services.Impl;

import java.math.BigDecimal;

import com.app.entites.User;
import com.app.exceptions.APIException;
import com.app.mappers.CartMapper;
import com.app.payloads.CartResponse;
import com.app.services.CartService;
import com.app.services.UserService;
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
	private final UserService userService;

	@Override
	@Transactional(readOnly = true)
	public CartResponse getCurrentUserCart() {
		Cart cart = getCurrentUserCartEntity();
		log.debug("Cart fetched successfully. cartId={}", cart.getCartId());
		return cartMapper.toResponse(cart);
	}

	@Override
	public CartResponse addProductToCart(Long productId, Integer quantity) {
		validateQuantity(quantity);
		Cart cart = getCurrentUserCartEntity();
		Product product = getProductEntity(productId);
		if (product.getQuantity() < quantity) {
			throw new APIException("Insufficient stock available");
		}

		CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(), productId);

		if (cartItem != null) {
			int newQuantity = cartItem.getQuantity() + quantity;
			if (newQuantity > product.getQuantity()) {
				throw new APIException("Insufficient stock available");
			}
			cartItem.setQuantity(newQuantity);
		}
		else
		{
			cartItem = new CartItem();
			cartItem.setCart(cart);
			cartItem.setProduct(product);
			cartItem.setQuantity(quantity);
			cartItem.setDiscount(product.getDiscount());
			cartItem.setProductPrice(product.getSpecialPrice());
			cart.getCartItems().add(cartItem);
		}

		recalculateCart(cart);
		Cart savedCart = cartRepository.save(cart);
		log.info("Product added to cart. cartId={}, productId={}, quantity={}",
				cart.getCartId(), productId, quantity);
		return cartMapper.toResponse(savedCart);
	}

	@Override
	public CartResponse updateCartItemQuantity(Long productId, Integer quantity) {

		validateQuantity(quantity);
		Cart cart = getCurrentUserCartEntity();

		CartItem cartItem = cartItemRepository
						.findCartItemByProductIdAndCartId(cart.getCartId(), productId);

		if (cartItem == null) {
			throw new ResourceNotFoundException("CartItem", "productId", productId);
		}

		Product product = cartItem.getProduct();

		if (quantity > product.getQuantity()) {
			throw new APIException("Insufficient stock available");
		}

		cartItem.setQuantity(quantity);
		recalculateCart(cart);
		Cart savedCart = cartRepository.save(cart);

		log.info("Cart item quantity updated. cartId={}, productId={}, quantity={}",
				cart.getCartId(),
				productId,
				quantity);
		return cartMapper.toResponse(savedCart);
	}

	@Override
	public void removeProductFromCart(Long productId) {
		Cart cart = getCurrentUserCartEntity();

		CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(), productId);

		if (cartItem == null) {
			throw new ResourceNotFoundException("CartItem", "productId", productId);
		}

		cart.getCartItems().remove(cartItem);
		recalculateCart(cart);
		cartRepository.save(cart);
		log.info("Product removed from cart. cartId={}, productId={}", cart.getCartId(), productId);
	}


	@Override
	public void refreshProductPriceInCart(Long productId) {
		log.debug("Refreshing cart item price. productId={}", productId);
		Cart cart = getCurrentUserCartEntity();
		CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(), productId);

		if (cartItem == null) {
			log.debug("Cart item not found while refreshing price. cartId={}, productId={}",
					cart.getCartId(),
					productId);
			return;
		}

		Product product = getProductEntity(productId);
		cartItem.setProductPrice(product.getSpecialPrice());
		cartItem.setDiscount(product.getDiscount());
		recalculateCart(cart);
		cartRepository.save(cart);
		log.debug("Cart item price refreshed successfully. cartId={}, productId={}",
				cart.getCartId(),
				productId);
	}


	private void validateQuantity(Integer quantity) {

		if (quantity == null || quantity <= 0) {
			throw new APIException("Quantity must be greater than zero");
		}
	}

	private Cart getCurrentUserCartEntity() {

		User user = userService.getAuthenticatedUserEntity();
		Cart cart = user.getCart();
		if (cart == null) {
			log.error("Cart missing for userId={}", user.getUserId());
			throw new APIException("Cart not found for current user");
		}

		return cart;
	}


	private void recalculateCart(Cart cart) {

		BigDecimal totalPrice = cart.getCartItems()
						.stream()
						.map(item ->
								(item.getProductPrice() == null
										? BigDecimal.ZERO
										: item.getProductPrice())
										.multiply(BigDecimal.valueOf(item.getQuantity())))
						.reduce(BigDecimal.ZERO, BigDecimal::add);

		cart.setTotalPrice(totalPrice);
		log.debug("Cart recalculated. cartId={}, totalPrice={}",
				cart.getCartId(),
				totalPrice);
	}

	private Product getProductEntity(Long productId) {
		return productRepository.findById(productId)
				.orElseThrow(() -> {
					log.warn("Product not found. productId={}", productId);
					return new ResourceNotFoundException("Product", "productId", productId);
				});
	}
}





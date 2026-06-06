package com.app.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import com.app.entites.CartItem;
import com.app.entites.Product;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

	@Query("SELECT ci.product FROM CartItem ci WHERE ci.product.productId = :productId")
	Product findProductById(@Param("productId") Long productId);

	@Query("SELECT ci FROM CartItem ci WHERE ci.cart.cartId = :cartId AND ci.product.productId = :productId")
	CartItem findCartItemByProductIdAndCartId(@Param("cartId") Long cartId,
	                                          @Param("productId") Long productId);

	@Modifying
	@Transactional
	@Query("DELETE FROM CartItem ci WHERE ci.cart.cartId = :cartId AND ci.product.productId = :productId")
	void deleteCartItemByProductIdAndCartId(@Param("cartId") Long cartId,
	                                        @Param("productId") Long productId);
}
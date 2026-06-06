package com.app.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.app.entites.Cart;

@Repository
public interface CartRepository
		extends JpaRepository<Cart, Long> {

	@EntityGraph(attributePaths = {
			"cartItems",
			"cartItems.product"
	})
    Optional<Cart> findCartByUserEmailAndCartId(
			String email,
			Long cartId
	);

	@Query("""
            SELECT DISTINCT c
            FROM Cart c
            JOIN FETCH c.cartItems ci
            JOIN FETCH ci.product p
            WHERE p.productId = :productId
            """)
	List<Cart> findCartsByProductId(
			@Param("productId")
			Long productId
	);
}
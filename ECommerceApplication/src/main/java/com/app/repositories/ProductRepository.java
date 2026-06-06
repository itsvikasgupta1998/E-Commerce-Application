package com.app.repositories;

import com.app.entites.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository
		extends JpaRepository<Product, Long> {

	boolean existsByProductNameIgnoreCase(
			String productName
	);

	Page<Product> findByCategoryCategoryId(
			Long categoryId,
			Pageable pageable
	);

	Page<Product> findByProductNameContainingIgnoreCase(
			String keyword,
			Pageable pageable
	);
}
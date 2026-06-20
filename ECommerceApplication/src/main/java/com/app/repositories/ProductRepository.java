package com.app.repositories;

import com.app.entites.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository
		extends JpaRepository<Product, Long> {

	Page<Product> findByCategoryCategoryId(
			Long categoryId,
			Pageable pageable
	);

	Page<Product> findByProductNameContainingIgnoreCase(
			String keyword,
			Pageable pageable
	);

	long countByCategory_CategoryId(Long categoryId);

	boolean existsBySkuIgnoreCase(String sku);

	boolean existsByProductNameIgnoreCase(String productName);

	Optional<Product> findByProductNameIgnoreCase(
			String productName
	);
}
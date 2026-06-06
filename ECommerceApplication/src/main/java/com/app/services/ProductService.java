package com.app.services;

import java.io.IOException;
import com.app.payloads.ProductPageResponse;
import com.app.payloads.UpdateProductRequest;
import org.springframework.web.multipart.MultipartFile;
import com.app.payloads.CreateProductRequest;
import com.app.payloads.ProductResponse;


public interface ProductService {

	ProductResponse createProduct(
			Long categoryId,
			CreateProductRequest request
	);

	ProductResponse getProductById(Long productId);

	ProductResponse updateProduct(
			Long productId,
			UpdateProductRequest request
	);

	ProductResponse updateProductImage(
			Long productId,
			MultipartFile image
	) throws IOException;

	void deleteProduct(Long productId);

	ProductPageResponse getAllProducts(
			int page,
			int size,
			String sortBy,
			String sortDir
	);

	ProductPageResponse getProductsByCategory(
			Long categoryId,
			int page,
			int size,
			String sortBy,
			String sortDir
	);

	ProductPageResponse searchProducts(
			String keyword,
			int page,
			int size,
			String sortBy,
			String sortDir
	);
}
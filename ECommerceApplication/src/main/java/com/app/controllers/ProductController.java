package com.app.controllers;

import com.app.payloads.*;
import com.app.services.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product APIs")
public class ProductController {

	private final ProductService productService;

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/category/{categoryId}")
	public ResponseEntity<ProductResponse> createProduct(
			@PathVariable Long categoryId,
			@Valid @RequestBody CreateProductRequest request
	) {

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(
						productService.createProduct(
								categoryId,
								request
						)
				);
	}

	@GetMapping("/{productId}")
	public ResponseEntity<ProductResponse> getProduct(
			@PathVariable Long productId
	) {

		return ResponseEntity.ok(
				productService.getProductById(productId)
		);
	}

	@GetMapping
	public ResponseEntity<ProductPageResponse> getAllProducts(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "productId") String sortBy,
			@RequestParam(defaultValue = "asc") String sortDir
	) {

		return ResponseEntity.ok(
				productService.getAllProducts(
						page,
						size,
						sortBy,
						sortDir
				)
		);
	}

	@GetMapping("/category/{categoryId}")
	public ResponseEntity<ProductPageResponse> getProductsByCategory(
			@PathVariable Long categoryId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "productId") String sortBy,
			@RequestParam(defaultValue = "asc") String sortDir
	) {

		return ResponseEntity.ok(
				productService.getProductsByCategory(
						categoryId,
						page,
						size,
						sortBy,
						sortDir
				)
		);
	}

	@GetMapping("/search")
	public ResponseEntity<ProductPageResponse> searchProducts(
			@RequestParam String keyword,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "productId") String sortBy,
			@RequestParam(defaultValue = "asc") String sortDir
	) {

		return ResponseEntity.ok(
				productService.searchProducts(
						keyword,
						page,
						size,
						sortBy,
						sortDir
				)
		);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/{productId}")
	public ResponseEntity<ProductResponse> updateProduct(
			@PathVariable Long productId,
			@Valid @RequestBody UpdateProductRequest request
	) {

		return ResponseEntity.ok(
				productService.updateProduct(
						productId,
						request
				)
		);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping(
			value = "/{productId}/image",
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE
	)
	public ResponseEntity<ProductResponse> updateProductImage(
			@PathVariable Long productId,
			@RequestParam("image") MultipartFile image
	) throws Exception {

		return ResponseEntity.ok(
				productService.updateProductImage(
						productId,
						image
				)
		);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{productId}")
	public ResponseEntity<APIResponse> deleteProduct(
			@PathVariable Long productId
	) {

		productService.deleteProduct(productId);

		return ResponseEntity.ok(
				APIResponse.builder()
						.message("Category deleted successfully")
						.success(true)
						.build()
		);
	}
}
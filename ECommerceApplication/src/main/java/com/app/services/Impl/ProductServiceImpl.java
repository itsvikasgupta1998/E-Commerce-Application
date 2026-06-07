package com.app.services.Impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import com.app.services.FileService;
import com.app.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import com.app.entites.Category;
import com.app.entites.Product;
import com.app.exceptions.ResourceNotFoundException;
import com.app.mappers.ProductMapper;
import com.app.payloads.*;
import com.app.repositories.CategoryRepository;
import com.app.repositories.ProductRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;
	private final CategoryRepository categoryRepository;
	private final ProductMapper productMapper;
	private final FileService fileService;

	// ---------------- CREATE PRODUCT ----------------
	@Override
	public ProductResponse createProduct(Long categoryId, CreateProductRequest request) {

		log.info("Create product request received for categoryId={}, productName={}",
				categoryId, request.getProductName());

		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> {
					log.error("Category not found: {}", categoryId);
					return new ResourceNotFoundException("Category", "categoryId", categoryId);
				});

		Product product = productMapper.toEntity(request);
		product.setCategory(category);
		product.setImage("default.png");

		product.setSpecialPrice(
				calculateSpecialPrice(request.getPrice(), request.getDiscount())
		);

		Product savedProduct = productRepository.save(product);

		log.info("Product created successfully. productId={}, categoryId={}",
				savedProduct.getProductId(), categoryId);

		return productMapper.toResponse(savedProduct);
	}

	// ---------------- GET PRODUCT BY ID ----------------
	@Override
	@Transactional(readOnly = true)
	public ProductResponse getProductById(Long productId) {

		log.info("Fetching product by id: {}", productId);

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> {
					log.error("Product not found: {}", productId);
					return new ResourceNotFoundException("Product", "productId", productId);
				});

		log.debug("Product fetched successfully: {}", product.getProductName());

		return productMapper.toResponse(product);
	}

	// ---------------- GET ALL PRODUCTS ----------------
	@Override
	@Transactional(readOnly = true)
	public ProductPageResponse getAllProducts(int page, int size, String sortBy, String sortDir) {

		log.info("Fetching all products. page={}, size={}, sortBy={}, sortDir={}",
				page, size, sortBy, sortDir);

		Sort sort = sortDir.equalsIgnoreCase("desc")
				? Sort.by(sortBy).descending()
				: Sort.by(sortBy).ascending();

		Pageable pageable = PageRequest.of(page, size, sort);

		Page<Product> productPage = productRepository.findAll(pageable);

		log.debug("Total products fetched: {}", productPage.getTotalElements());

		List<ProductResponse> content = productPage.getContent()
				.stream()
				.map(productMapper::toResponse)
				.toList();

		return ProductPageResponse.builder()
				.content(content)
				.pageNumber(productPage.getNumber())
				.pageSize(productPage.getSize())
				.totalElements(productPage.getTotalElements())
				.totalPages(productPage.getTotalPages())
				.lastPage(productPage.isLast())
				.build();
	}

	// ---------------- UPDATE PRODUCT ----------------
	@Override
	public ProductResponse updateProduct(Long productId, UpdateProductRequest request) {

		log.info("Update product request received. productId={}", productId);

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> {
					log.error("Product not found for update: {}", productId);
					return new ResourceNotFoundException("Product", "productId", productId);
				});

		productMapper.updateEntity(request, product);

		if (request.getPrice() != null || request.getDiscount() != null) {
			product.setSpecialPrice(
					calculateSpecialPrice(product.getPrice(), product.getDiscount())
			);
		}

		Product updated = productRepository.save(product);

		log.info("Product updated successfully. productId={}", updated.getProductId());

		return productMapper.toResponse(updated);
	}

	// ---------------- UPDATE PRODUCT IMAGE ----------------
	@Override
	public ProductResponse updateProductImage(Long productId, MultipartFile image) throws IOException {

		log.info("Update product image request. productId={}, file={}",
				productId, image != null ? image.getOriginalFilename() : null);

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> {
					log.error("Product not found for image update: {}", productId);
					return new ResourceNotFoundException("Product", "productId", productId);
				});

		if (product.getImage() != null &&
				!product.getImage().isBlank() &&
				!"default.png".equals(product.getImage())) {

			log.debug("Deleting old image: {}", product.getImage());
			fileService.deleteImage(product.getImage());
		}

		String fileName = fileService.uploadImage(image);

		product.setImage(fileName);

		Product updatedProduct = productRepository.save(product);

		log.info("Product image updated successfully. productId={}, image={}",
				productId, fileName);

		return productMapper.toResponse(updatedProduct);
	}

	// ---------------- DELETE PRODUCT ----------------
	@Override
	public void deleteProduct(Long productId) {

		log.info("Delete product request received: {}", productId);

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> {
					log.error("Product not found for deletion: {}", productId);
					return new ResourceNotFoundException("Product", "productId", productId);
				});

		productRepository.delete(product);

		log.info("Product deleted successfully: {}", productId);
	}

	// ---------------- CATEGORY PRODUCTS ----------------
	@Override
	@Transactional(readOnly = true)
	public ProductPageResponse getProductsByCategory(Long categoryId, int page, int size, String sortBy, String sortDir) {

		log.info("Fetching products by categoryId={}", categoryId);

		categoryRepository.findById(categoryId)
				.orElseThrow(() -> {
					log.error("Category not found with Id: {}", categoryId);
					return new ResourceNotFoundException("Category", "categoryId", categoryId);
				});

		Sort sort = sortDir.equalsIgnoreCase("desc")
				? Sort.by(sortBy).descending()
				: Sort.by(sortBy).ascending();

		Pageable pageable = PageRequest.of(page, size, sort);

		Page<Product> productPage = productRepository.findByCategoryCategoryId(categoryId, pageable);

		log.debug("Products found for categoryId {}: {}", categoryId, productPage.getTotalElements());

		List<ProductResponse> content = productPage.getContent()
				.stream()
				.map(productMapper::toResponse)
				.toList();

		return ProductPageResponse.builder()
				.content(content)
				.pageNumber(productPage.getNumber())
				.pageSize(productPage.getSize())
				.totalElements(productPage.getTotalElements())
				.totalPages(productPage.getTotalPages())
				.lastPage(productPage.isLast())
				.build();
	}

	// ---------------- SEARCH PRODUCTS ----------------
	@Override
	@Transactional(readOnly = true)
	public ProductPageResponse searchProducts(String keyword, int page, int size, String sortBy, String sortDir) {

		log.info("Search products request. keyword={}", keyword);

		Sort sort = sortDir.equalsIgnoreCase("desc")
				? Sort.by(sortBy).descending()
				: Sort.by(sortBy).ascending();

		Pageable pageable = PageRequest.of(page, size, sort);

		Page<Product> productPage =
				productRepository.findByProductNameContainingIgnoreCase(keyword, pageable);

		log.debug("Search results count: {}", productPage.getTotalElements());

		List<ProductResponse> content = productPage.getContent()
				.stream()
				.map(productMapper::toResponse)
				.toList();

		return ProductPageResponse.builder()
				.content(content)
				.pageNumber(productPage.getNumber())
				.pageSize(productPage.getSize())
				.totalElements(productPage.getTotalElements())
				.totalPages(productPage.getTotalPages())
				.lastPage(productPage.isLast())
				.build();
	}

	// ---------------- PRICE CALCULATION ----------------
	private BigDecimal calculateSpecialPrice(BigDecimal price, BigDecimal discount) {

		if (price == null) {
			return BigDecimal.ZERO;
		}

		if (discount == null) {
			discount = BigDecimal.ZERO;
		}

		BigDecimal discountAmount = price.multiply(
				discount.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
		);

		return price.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
	}
}
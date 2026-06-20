package com.app.services.Impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import com.app.exceptions.APIException;
import com.app.services.FileService;
import com.app.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
	private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;

	// ---------------- CREATE PRODUCT ----------------
	@Override
	public ProductResponse createProduct(Long categoryId,
	                                     CreateProductRequest request) {

		log.info("Create product request received for categoryId={}, productName={}",
				categoryId, request.getProductName());

		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Category",
						"categoryId",
						categoryId
				));

		BigDecimal discount =
				request.getDiscount() == null
						? BigDecimal.ZERO
						: request.getDiscount();


		String productName = request.getProductName().trim();
		String sku = request.getSku()
				.trim()
				.toUpperCase();

		if (productRepository.existsByProductNameIgnoreCase(productName)) {

			throw new APIException(
					"Product already exists"
			);
		}

		if (productRepository.existsBySkuIgnoreCase(sku)) {

			throw new APIException(
					"SKU already exists"
			);
		}

		if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {

			throw new APIException(
					"Price must be greater than zero"
			);
		}

		if (discount.compareTo(BigDecimal.ZERO) < 0
				|| discount.compareTo(BigDecimal.valueOf(100)) > 0) {

			throw new APIException(
					"Discount must be between 0 and 100"
			);
		}
		if (request.getQuantity() < 0) {

			throw new APIException(
					"Quantity cannot be negative"
			);
		}

		Product product = productMapper.toEntity(request);

		product.setProductName(productName);
		product.setSku(sku);
		product.setCategory(category);
		product.setImage("default.png");
		product.setDiscount(discount);

		product.setSpecialPrice(
				calculateSpecialPrice(
						request.getPrice(),
						discount
				)
		);

		try
		{
			productRepository.save(product);
		}
		catch(DataIntegrityViolationException ex){
			throw new APIException("SKU already exists");
		}

		log.info("Product created successfully. productId={}",
				product.getProductId());

		return productMapper.toResponse(product);

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

		Pageable pageable = createPageable(page, size, sortBy, sortDir);

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

		log.info("Update product request received. productId={}",
				productId);

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Product",
						"productId",
						productId
				));

		productMapper.updateEntity(request, product);

		if (product.getDiscount() == null) {
			product.setDiscount(BigDecimal.ZERO);
		}

		if (request.getProductName() != null) {

			String productName = request.getProductName().trim();

			productRepository
					.findByProductNameIgnoreCase(productName)
					.ifPresent(existing -> {

						if (!existing.getProductId()
								.equals(productId)) {

							throw new APIException(
									"Product already exists"
							);
						}
					});

			product.setProductName(productName);
		}

		if (product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {

			throw new APIException(
					"Price must be greater than zero"
			);
		}

		if (product.getDiscount().compareTo(BigDecimal.ZERO) < 0
				|| product.getDiscount().compareTo(BigDecimal.valueOf(100)) > 0) {

			throw new APIException(
					"Discount must be between 0 and 100"
			);
		}
		if (product.getQuantity() < 0) {

			throw new APIException(
					"Quantity cannot be negative"
			);
		}

		product.setSpecialPrice(
				calculateSpecialPrice(
						product.getPrice(),
						product.getDiscount()
				)
		);

		Product updated = productRepository.save(product);

		log.info("Product updated successfully. productId={}",
				updated.getProductId());

		return productMapper.toResponse(updated);
	}

	// ---------------- UPDATE PRODUCT IMAGE ----------------
	@Transactional
	@Override
	public ProductResponse updateProductImage(Long productId,
	                                          MultipartFile image)
			throws IOException {

		log.info("Update product image request. productId={}",
				productId);

		if (image == null || image.isEmpty()) {

			throw new APIException(
					"Image file is required"
			);
		}

		String fileName = image.getOriginalFilename();

		if (fileName == null || fileName.isBlank()) {
			throw new APIException("Invalid image file name");
		}

		String lowerFileName = fileName.toLowerCase();

		if (!(lowerFileName.endsWith(".jpg")
				|| lowerFileName.endsWith(".jpeg")
				|| lowerFileName.endsWith(".png")
				|| lowerFileName.endsWith(".webp"))) {

			throw new APIException(
					"Only JPG, JPEG, PNG and WEBP images are allowed"
			);
		}

		String contentType = image.getContentType();

		Set<String> allowedContentTypes = Set.of(
				"image/jpeg",
				"image/png",
				"image/webp"
		);

		if (contentType == null || !allowedContentTypes.contains(contentType)) {
			throw new APIException("Invalid image content type");
		}

		if (image.getSize() > MAX_IMAGE_SIZE) {

			throw new APIException(
					"Image size cannot exceed 5 MB"
			);
		}

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Product",
						"productId",
						productId
				));

		String oldImage = product.getImage();

		String uploadedImage =
				fileService.uploadImage(image);
		if (uploadedImage == null || uploadedImage.isBlank()) {
			throw new APIException("Image upload failed");
		}

		product.setImage(uploadedImage);

		Product updated = productRepository.save(product);

		if (oldImage != null
				&& !oldImage.isBlank()
				&& !"default.png".equals(oldImage)) {

			try {

				fileService.deleteImage(oldImage);

			} catch (IOException ex) {

				log.warn(
						"Failed to delete old image {}",
						oldImage,
						ex
				);
			}
		}

		log.info("Product image updated successfully. productId={}",
				productId);

		ProductResponse response = productMapper.toResponse(updated);
		response.setImageUrl(
				"http://localhost:8080/api/files/" + updated.getImage()
		);

		return response;
	}


	// ---------------- DELETE PRODUCT ----------------
	@Override
	public void deleteProduct(Long productId) {

		log.info("Delete product request received. productId={}",
				productId);

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Product",
						"productId",
						productId
				));

		String imageName = product.getImage();

		productRepository.delete(product);

		if (imageName != null
				&& !imageName.isBlank()
				&& !"default.png".equals(imageName)) {

			try {

				fileService.deleteImage(imageName);

			} catch (IOException ex) {

				log.error(
						"Failed to delete image {}",
						imageName,
						ex
				);
			}
		}

		log.info("Product deleted successfully. productId={}",
				productId);
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

		Pageable pageable = createPageable(page, size, sortBy, sortDir);

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

		if (keyword == null) {
			throw new APIException("Keyword is required");
		}
		keyword = keyword.trim();
		if (keyword.isBlank())
		{
			throw new APIException( "Keyword cannot be blank" );
		}
		if (keyword.length() > 100)
		{
			throw new APIException( "Keyword too long" );
		}

		Pageable pageable = createPageable(page, size, sortBy, sortDir);

		Page<Product> productPage = productRepository.findByProductNameContainingIgnoreCase(keyword, pageable);

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


	private void validateSortField(String sortBy) {

		if (sortBy == null || sortBy.isBlank()) {
			throw new APIException("Sort field is required");
		}
		sortBy = sortBy.trim();
		Set<String> allowedFields = Set.of(
				"productId",
				"productName",
				"price",
				"specialPrice",
				"quantity",
				"createdAt",
				"updatedAt"
		);

		if (!allowedFields.contains(sortBy)) {

			throw new APIException(
					"Invalid sort field"
			);
		}
	}

	private void validatePageRequest(
			int page,
			int size
	) {

		if (page < 0) {

			throw new APIException(
					"Page number cannot be negative"
			);
		}

		if (size < 1 || size > 100) {

			throw new APIException(
					"Page size must be between 1 and 100"
			);
		}
	}

	private void validateSortDirection(String sortDir) {

		if (sortDir == null
				|| (!sortDir.equalsIgnoreCase("asc")
				&& !sortDir.equalsIgnoreCase("desc"))) {

			throw new APIException(
					"Sort direction must be asc or desc"
			);
		}
	}

	private Pageable createPageable(
			int page,
			int size,
			String sortBy,
			String sortDir
	) {

		validatePageRequest(page, size);
		validateSortField(sortBy);
		validateSortDirection(sortDir);

		Sort sort = "desc".equalsIgnoreCase(sortDir)
				? Sort.by(sortBy).descending()
				: Sort.by(sortBy).ascending();

		return PageRequest.of(page, size, sort);
	}
}
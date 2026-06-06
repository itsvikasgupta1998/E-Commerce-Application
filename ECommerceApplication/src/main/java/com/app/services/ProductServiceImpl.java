package com.app.services;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.List;
import com.app.mappers.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.app.entites.Category;
import com.app.entites.Product;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.CreateProductRequest;
import com.app.payloads.ProductResponse;
import com.app.repositories.CategoryRepository;
import com.app.repositories.ProductRepository;
import org.springframework.transaction.annotation.Transactional;
import com.app.payloads.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;
	private final CategoryRepository categoryRepository;
	private final ProductMapper productMapper;
	private final FileService fileService;

	@Override
	public ProductResponse createProduct(
			Long categoryId,
			CreateProductRequest request
	) {

		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() ->
						new ResourceNotFoundException(
								"Category",
								"categoryId",
								categoryId
						));

		Product product = productMapper.toEntity(request);

		product.setCategory(category);

		product.setImage("default.png");

		product.setSpecialPrice(
				calculateSpecialPrice(
						request.getPrice(),
						request.getDiscount()
				)
		);

		Product savedProduct = productRepository.save(product);
		log.info(
				"Product created : {}",
				savedProduct.getProductId()
		);
		return productMapper.toResponse(savedProduct);

	}


	@Override
	@Transactional(readOnly = true)
	public ProductResponse getProductById(
			Long productId
	) {

		Product product =
				productRepository.findById(productId)
						.orElseThrow(() ->
								new ResourceNotFoundException(
										"Product",
										"productId",
										productId
								));

		return productMapper.toResponse(product);
	}

	@Override
	@Transactional(readOnly = true)
	public ProductPageResponse getAllProducts(
			int page,
			int size,
			String sortBy,
			String sortDir
	) {

		Sort sort = sortDir.equalsIgnoreCase("desc")
				? Sort.by(sortBy).descending()
				: Sort.by(sortBy).ascending();

		Pageable pageable =
				PageRequest.of(page, size, sort);

		Page<Product> productPage =
				productRepository.findAll(pageable);

		List<ProductResponse> content =
				productPage.getContent()
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

	@Override
	@Transactional(readOnly = true)
	public ProductPageResponse getProductsByCategory(
			Long categoryId,
			int page,
			int size,
			String sortBy,
			String sortDir
	) {

		categoryRepository.findById(categoryId)
				.orElseThrow(() ->
						new ResourceNotFoundException(
								"Category",
								"categoryId",
								categoryId
						));

		Sort sort = sortDir.equalsIgnoreCase("desc")
				? Sort.by(sortBy).descending()
				: Sort.by(sortBy).ascending();

		Pageable pageable =
				PageRequest.of(page, size, sort);

		Page<Product> productPage =
				productRepository.findByCategoryCategoryId(
						categoryId,
						pageable
				);

		List<ProductResponse> content =
				productPage.getContent()
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

	@Override
	@Transactional(readOnly = true)
	public ProductPageResponse searchProducts(
			String keyword,
			int page,
			int size,
			String sortBy,
			String sortDir
	) {

		Sort sort = sortDir.equalsIgnoreCase("desc")
				? Sort.by(sortBy).descending()
				: Sort.by(sortBy).ascending();

		Pageable pageable =
				PageRequest.of(page, size, sort);

		Page<Product> productPage =
				productRepository
						.findByProductNameContainingIgnoreCase(
								keyword,
								pageable
						);

		List<ProductResponse> content =
				productPage.getContent()
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

	@Override
	public ProductResponse updateProduct(
			Long productId,
			UpdateProductRequest request
	) {

		Product product =
				productRepository.findById(productId)
						.orElseThrow(() ->
								new ResourceNotFoundException(
										"Product",
										"productId",
										productId
								));

		productMapper.updateEntity(
				request,
				product
		);

		if (request.getPrice() != null ||
				request.getDiscount() != null) {

			product.setSpecialPrice(
					calculateSpecialPrice(
							product.getPrice(),
							product.getDiscount()
					)
			);
		}

		Product updated =
				productRepository.save(product);

		return productMapper.toResponse(updated);
	}

	@Override
	public ProductResponse updateProductImage(
			Long productId,
			MultipartFile image
	) throws IOException {

		Product product =
				productRepository.findById(productId)
						.orElseThrow(() ->
								new ResourceNotFoundException(
										"Product",
										"productId",
										productId
								));

		// Delete old image first
		if (product.getImage() != null
				&& !product.getImage().isBlank()
				&& !"default.png".equals(product.getImage())) {

			fileService.deleteImage(
					product.getImage()
			);
		}

		String fileName =
				fileService.uploadImage(image);

		product.setImage(fileName);

		Product updatedProduct =
				productRepository.save(product);

		return productMapper.toResponse(
				updatedProduct
		);
	}

	@Override
	public void deleteProduct(
			Long productId
	) {

		Product product =
				productRepository.findById(productId)
						.orElseThrow(() ->
								new ResourceNotFoundException(
										"Product",
										"productId",
										productId
								));

		productRepository.delete(product);
		log.info(
				"Product deleted : {}",
				productId
		);
	}

	private BigDecimal calculateSpecialPrice(
			BigDecimal price,
			BigDecimal discount
	) {

		if (price == null) {
			return BigDecimal.ZERO;
		}

		if (discount == null) {
			discount = BigDecimal.ZERO;
		}

		BigDecimal discountAmount =
				price.multiply(
						discount.divide(
								BigDecimal.valueOf(100),
								2,
								RoundingMode.HALF_UP
						)
				);

		return price.subtract(discountAmount)
				.setScale(2, RoundingMode.HALF_UP);
	}
}
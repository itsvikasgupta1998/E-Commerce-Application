package com.app.services.Impl;

import com.app.exceptions.APIException;
import com.app.mappers.CategoryMapper;
import com.app.payloads.CategoryPageResponse;
import com.app.payloads.UpdateCategoryRequest;
import com.app.repositories.ProductRepository;
import com.app.services.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.app.entites.Category;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.CreateCategoryRequest;
import com.app.payloads.CategoryResponse;
import com.app.repositories.CategoryRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

	private final CategoryRepository categoryRepository;
	private final CategoryMapper categoryMapper;
	private final ProductRepository productRepository;

	@Override
	public CategoryResponse createCategory(CreateCategoryRequest request) {
		log.info("Creating category with name: {}", request.getCategoryName());
		String categoryName = request.getCategoryName().trim();
		categoryRepository.findByCategoryNameIgnoreCase(categoryName)
				.ifPresent(existing -> {
					throw new APIException("Category already exists");
				});

		Category category = categoryMapper.toEntity(request);
		category.setCategoryName(categoryName);
		Category saved = categoryRepository.save(category);
		log.info("Category created successfully with id: {}", saved.getCategoryId());
		return categoryMapper.toResponse(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public CategoryResponse getCategoryById(Long categoryId) {
		log.info("Fetching category with id: {}", categoryId);
		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> {
					log.error("Category not found with id: {}", categoryId);
					return new ResourceNotFoundException("Category", "categoryId", categoryId);
				});

		log.debug("Category fetched successfully: {}", category.getCategoryName());
		return categoryMapper.toResponse(category);
	}

	@Override
	public CategoryResponse updateCategory(Long categoryId, UpdateCategoryRequest request
	) {

		log.info("Updating category with id: {}", categoryId);
		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

		String categoryName = request.getCategoryName().trim();
		categoryRepository.findByCategoryNameIgnoreCase(categoryName)
				.ifPresent(existing ->
				{
					if (!existing.getCategoryId().equals(categoryId))
					{
						throw new APIException("Category already exists");
					}
				});
		category.setCategoryName(categoryName);
		Category updated = categoryRepository.save(category);
		return categoryMapper.toResponse(updated);
	}

	@Override
	public void deleteCategory(Long categoryId) {
		log.info("Deleting category with id: {}", categoryId);
		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> {
					log.error("Category not found for deletion: {}", categoryId);
					return new ResourceNotFoundException("Category", "categoryId", categoryId);
				});

		long productCount = productRepository.countByCategory_CategoryId(categoryId);
		if (productCount > 0) {
			throw new APIException("Cannot delete category because products exist in it.");
		}

		categoryRepository.delete(category);
		log.info("Category deleted successfully with id: {}", categoryId);
	}

	@Override
	@Transactional(readOnly = true)
	public CategoryPageResponse getAllCategories(int page, int size, String sortBy, String sortDir) {
		log.info("Fetching categories - page: {}, size: {}, sortBy: {}, sortDir: {}",
				page, size, sortBy, sortDir);

		Set<String> allowedFields = Set.of("categoryId", "categoryName", "createdAt", "updatedAt");

		if (!allowedFields.contains(sortBy)) {
			throw new APIException("Invalid sort field: " + sortBy);
		}

		Sort sort = sortDir.equalsIgnoreCase("desc")
				? Sort.by(sortBy).descending()
				: Sort.by(sortBy).ascending();

		Page<Category> categoryPage = categoryRepository.findAll(PageRequest.of(page, size, sort));
		log.debug("Fetched {} categories from DB", categoryPage.getTotalElements());
		CategoryPageResponse response = CategoryPageResponse.builder()
				.content(categoryPage.getContent()
						.stream()
						.map(categoryMapper::toResponse)
						.toList())
				.pageNumber(categoryPage.getNumber())
				.pageSize(categoryPage.getSize())
				.totalElements(categoryPage.getTotalElements())
				.totalPages(categoryPage.getTotalPages())
				.lastPage(categoryPage.isLast())
				.build();

		log.info("Category pagination response prepared successfully");
		return response;
	}
}

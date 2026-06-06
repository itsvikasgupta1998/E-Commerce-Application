package com.app.services;

import com.app.mappers.CategoryMapper;
import com.app.payloads.CategoryPageResponse;
import com.app.payloads.UpdateCategoryRequest;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

	private final CategoryRepository categoryRepository;
	private final CategoryMapper categoryMapper;

	@Override
	public CategoryResponse createCategory(
			CreateCategoryRequest request
	) {

		categoryRepository
				.findByCategoryName(request.getCategoryName())
				.ifPresent(category -> {
					throw new IllegalStateException(
							"Category already exists"
					);
				});

		Category category =
				categoryMapper.toEntity(request);

		Category saved =
				categoryRepository.save(category);

		return categoryMapper.toResponse(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public CategoryResponse getCategoryById(
			Long categoryId
	) {

		Category category =
				categoryRepository.findById(categoryId)
						.orElseThrow(() ->
								new ResourceNotFoundException(
										"Category",
										"categoryId",
										categoryId
								));

		return categoryMapper.toResponse(category);
	}

	@Override
	public CategoryResponse updateCategory(
			Long categoryId,
			UpdateCategoryRequest request
	) {

		Category category =
				categoryRepository.findById(categoryId)
						.orElseThrow(() ->
								new ResourceNotFoundException(
										"Category",
										"categoryId",
										categoryId
								));

		categoryMapper.updateEntity(
				request,
				category
		);

		Category updated =
				categoryRepository.save(category);

		return categoryMapper.toResponse(updated);
	}

	@Override
	public void deleteCategory(
			Long categoryId
	) {

		Category category =
				categoryRepository.findById(categoryId)
						.orElseThrow(() ->
								new ResourceNotFoundException(
										"Category",
										"categoryId",
										categoryId
								));

		categoryRepository.delete(category);
	}

	@Override
	@Transactional(readOnly = true)
	public CategoryPageResponse getAllCategories(
			int page,
			int size,
			String sortBy,
			String sortDir
	) {

		Sort sort =
				sortDir.equalsIgnoreCase("desc")
						? Sort.by(sortBy).descending()
						: Sort.by(sortBy).ascending();

		Page<Category> categoryPage =
				categoryRepository.findAll(
						PageRequest.of(page, size, sort)
				);

		return CategoryPageResponse.builder()
				.content(
						categoryPage.getContent()
								.stream()
								.map(categoryMapper::toResponse)
								.toList()
				)
				.pageNumber(categoryPage.getNumber())
				.pageSize(categoryPage.getSize())
				.totalElements(categoryPage.getTotalElements())
				.totalPages(categoryPage.getTotalPages())
				.lastPage(categoryPage.isLast())
				.build();
	}



}

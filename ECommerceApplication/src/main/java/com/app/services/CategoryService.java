package com.app.services;


import com.app.payloads.CategoryPageResponse;
import com.app.payloads.CreateCategoryRequest;
import com.app.payloads.CategoryResponse;
import com.app.payloads.UpdateCategoryRequest;

public interface CategoryService {

	CategoryResponse createCategory(
			CreateCategoryRequest request
	);

	CategoryResponse getCategoryById(
			Long categoryId
	);

	CategoryPageResponse getAllCategories(
			int page,
			int size,
			String sortBy,
			String sortDir
	);

	CategoryResponse updateCategory(
			Long categoryId,
			UpdateCategoryRequest request
	);

	void deleteCategory(
			Long categoryId
	);
}
package com.app.controllers;


import com.app.payloads.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.app.services.CategoryService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category APIs")
public class CategoryController {

	private final CategoryService categoryService;

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping
	public ResponseEntity<CategoryResponse> createCategory(
			@Valid @RequestBody CreateCategoryRequest request
	) {

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(
						categoryService.createCategory(request)
				);
	}

	@GetMapping("/{categoryId}")
	public ResponseEntity<CategoryResponse> getCategory(
			@PathVariable Long categoryId
	) {

		return ResponseEntity.ok(
				categoryService.getCategoryById(categoryId)
		);
	}

	@GetMapping
	public ResponseEntity<CategoryPageResponse> getCategories(
			@RequestParam(defaultValue = "0")
			int page,

			@RequestParam(defaultValue = "10")
			int size,

			@RequestParam(defaultValue = "categoryId")
			String sortBy,

			@RequestParam(defaultValue = "asc")
			String sortDir
	) {

		return ResponseEntity.ok(
				categoryService.getAllCategories(
						page,
						size,
						sortBy,
						sortDir
				)
		);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/{categoryId}")
	public ResponseEntity<CategoryResponse> updateCategory(
			@PathVariable Long categoryId,
			@Valid @RequestBody UpdateCategoryRequest request
	) {

		return ResponseEntity.ok(
				categoryService.updateCategory(
						categoryId,
						request
				)
		);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{categoryId}")
	public ResponseEntity<APIResponse> deleteCategory(
			@PathVariable Long categoryId
	) {

		categoryService.deleteCategory(categoryId);

		return ResponseEntity.ok(
				APIResponse.builder()
						.message("Category deleted successfully")
						.success(true)
						.build()
		);
	}
}

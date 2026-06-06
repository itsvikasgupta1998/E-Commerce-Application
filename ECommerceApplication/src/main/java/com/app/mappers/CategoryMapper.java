package com.app.mappers;

import com.app.entites.Category;
import com.app.payloads.CreateCategoryRequest;
import com.app.payloads.CategoryResponse;
import com.app.payloads.UpdateCategoryRequest;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toEntity(CreateCategoryRequest request);

    CategoryResponse toResponse(Category category);

    @BeanMapping(
            nullValuePropertyMappingStrategy =
                    NullValuePropertyMappingStrategy.IGNORE
    )
    void updateEntity(
            UpdateCategoryRequest request,
            @MappingTarget Category category
    );
}

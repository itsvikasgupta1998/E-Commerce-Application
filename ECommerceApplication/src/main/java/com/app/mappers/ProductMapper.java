package com.app.mappers;

import com.app.entites.Product;
import com.app.payloads.CreateProductRequest;
import com.app.payloads.ProductResponse;
import com.app.payloads.UpdateProductRequest;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        uses = CategoryMapper.class
)
public interface ProductMapper {

    Product toEntity(CreateProductRequest request);

    ProductResponse toResponse(Product product);

    @BeanMapping(
            nullValuePropertyMappingStrategy =
                    NullValuePropertyMappingStrategy.IGNORE
    )
    void updateEntity(
            UpdateProductRequest request,
            @MappingTarget Product product
    );
}
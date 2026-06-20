package com.app.payloads;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

    @NotBlank
    private String productName;

    @NotBlank
    private String description;

    @PositiveOrZero
    private Integer quantity;

    @DecimalMin("0.0")
    private BigDecimal price;

    @DecimalMin("0.0")
    private BigDecimal discount;
}
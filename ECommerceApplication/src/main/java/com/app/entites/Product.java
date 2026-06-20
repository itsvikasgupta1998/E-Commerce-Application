package com.app.entites;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
		name = "products",
		indexes = {
				@Index(
						name = "idx_product_name",
						columnList = "product_name"
				),
				@Index(
						name = "idx_category",
						columnList = "category_id"
				),
				@Index(
						name = "idx_sku",
						columnList = "sku"
				)
		}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long productId;

	@Column(nullable = false,length = 200)
	private String productName;

	@Column(nullable = false,length = 1000)
	private String description;

	@Column(nullable = false)
	private Integer quantity;

	@Column(nullable = false,precision = 19,scale = 2)
	private BigDecimal price;

	@Column(nullable = false,precision = 5,scale = 2)
	private BigDecimal discount;

	@Column(nullable = false,precision = 19,scale = 2)
	private BigDecimal specialPrice;

	@Column(
			nullable = false,
			unique = true,
			length = 50
	)
	private String sku;

	@Column(
			nullable = false,
			length = 500
	)
	private String image;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "category_id",
			nullable = false
	)
	private Category category;

	@Version
	private Long version;

	@PrePersist
	@PreUpdate
	private void normalizeFields() {

		if (sku != null) {
			sku = sku.trim().toUpperCase();
		}

		if (productName != null) {
			productName = productName.trim();
		}
	}
	
}



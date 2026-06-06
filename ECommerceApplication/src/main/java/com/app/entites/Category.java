package com.app.entites;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;


@Entity
@Table(
		name = "categories",
		uniqueConstraints = {
				@UniqueConstraint(
						columnNames = "category_name"
				)
		}
)
@Data
public class Category extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long categoryId;

	@Column(
			name = "category_name",
			nullable = false,
			unique = true,
			length = 100
	)
	private String categoryName;

	@OneToMany(
			mappedBy = "category"
	)
	private List<Product> products;
}
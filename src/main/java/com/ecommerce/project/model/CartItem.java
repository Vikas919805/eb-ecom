package com.ecommerce.project.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name="cart_Items")
public class CartItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long cartItemId;
	
	@ManyToOne
	@JoinColumn(name = "cart_Id")
	private Cart cart;
	
	@ManyToOne
	@JoinColumn(name = "product_Id")
	private Product product;
	
	private Integer quantity;
	private Double discount;
	private Double productPrice;
	
	
	
	
}

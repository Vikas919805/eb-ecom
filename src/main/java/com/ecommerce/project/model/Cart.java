package com.ecommerce.project.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "carts")
public class Cart {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long cartId;
	
	@OneToOne
	@JoinColumn(name="user_Id")
	private User user;
	
	@OneToMany(mappedBy="cart",cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},orphanRemoval=true)
	private List<CartItem>cartItems =new ArrayList<>();
    private Double totalPrice = 0.0;
}

package com.ecommerce.project.payload;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CartItemDTO {
private long cartItemId;
private CartDTO cart;
private ProductDTO productDTO;
private Integer quantity;
private Double discount;
private Double productPrice;


}

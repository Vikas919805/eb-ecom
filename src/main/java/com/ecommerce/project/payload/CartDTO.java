package com.ecommerce.project.payload;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CartDTO {
private Long cartId;
private Double totalPrice = 0.0;
private List<ProductDTO>product = new ArrayList<>();
}

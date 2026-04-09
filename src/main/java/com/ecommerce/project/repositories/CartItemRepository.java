package com.ecommerce.project.repositories;

import com.ecommerce.project.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    CartItem findByProduct_ProductIdAndCart_CartId(Long productId, Long cartId);

    void deleteByProduct_ProductIdAndCart_CartId(Long productId, Long cartId);
}
package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private AuthUtil authUtil;

	@Autowired
	ProductRepository productRepository;

	@Autowired
	CartItemRepository cartItemRepository;

	@Autowired
	ModelMapper modelMapper;

	@Override
	public CartDTO addProductToCart(Long productId, Integer quantity) {
		Cart cart = createCart();

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

		CartItem cartItem = cartItemRepository.findByProduct_ProductIdAndCart_CartId(productId, cart.getCartId());

		if (cartItem != null) {
			throw new APIException("Product " + product.getProductName() + " already exists in the cart");
		}

		if (product.getQuantity() == 0) {
			throw new APIException(product.getProductName() + " is not available");
		}

		if (product.getQuantity() < quantity) {
			throw new APIException("Please, make an order of the " + product.getProductName()
					+ " less than or equal to the quantity " + product.getQuantity() + ".");
		}

		CartItem newCartItem = new CartItem();
		newCartItem.setProduct(product);
		newCartItem.setCart(cart);
		newCartItem.setQuantity(quantity);
		newCartItem.setDiscount(product.getDiscount());
		newCartItem.setProductPrice(product.getSpecialPrice());

		cartItemRepository.save(newCartItem);

		cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));
		cartRepository.save(cart);

		CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

		List<CartItem> cartItems = cart.getCartItems();

		Stream<ProductDTO> productStream = cartItems.stream().map(item -> {
			ProductDTO map = modelMapper.map(item.getProduct(), ProductDTO.class);
			map.setQuantity(item.getQuantity());
			return map;
		});

		cartDTO.setProduct(productStream.toList());
		return cartDTO;
	}

	@Override
	public List<CartDTO> getAllCarts() {
		List<Cart> carts = cartRepository.findAll();

		if (carts.isEmpty()) {
			throw new APIException("No cart exists");
		}

		return carts.stream().map(cart -> {
			CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

			List<ProductDTO> products = cart.getCartItems().stream()
					.map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).collect(Collectors.toList());

			cartDTO.setProduct(products);
			return cartDTO;
		}).collect(Collectors.toList());
	}

	@Override
	public CartDTO getCart(String emailId, Long cartId) {

	    Cart cart = cartRepository.findCartByEmailAndCartId(emailId, cartId)
	            .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

	    CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

	    cart.getCartItems().forEach(c -> 
	        c.getProduct().setQuantity(c.getQuantity())
	    );

	    List<ProductDTO> products = cart.getCartItems().stream()
	            .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class))
	            .toList();

	    cartDTO.setProduct(products);

	    return cartDTO;
	}

	@Transactional
	@Override
	public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {
		String emailId = authUtil.loggedInEmail();
		Cart userCart = cartRepository.findCartByEmail(emailId);
		Long cartId = userCart.getCartId();

		Cart cart = cartRepository.findById(cartId)
				.orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

		if (product.getQuantity() == 0) {
			throw new APIException(product.getProductName() + " is not available");
		}

		if (product.getQuantity() < quantity) {
			throw new APIException("Please, make an order of the " + product.getProductName()
					+ " less than or equal to the quantity " + product.getQuantity() + ".");
		}

		CartItem cartItem = cartItemRepository.findByProduct_ProductIdAndCart_CartId(productId, cartId);

		if (cartItem == null) {
			throw new APIException("Product " + product.getProductName() + " not available in the cart!!!");
		}

		int newQuantity = cartItem.getQuantity() + quantity;

		if (newQuantity < 0) {
			throw new APIException("The resulting quantity cannot be negative.");
		}

		if (newQuantity == 0) {
			deleteProductFromCart(cartId, productId);
		} else {
			cartItem.setProductPrice(product.getSpecialPrice());
			cartItem.setQuantity(newQuantity);
			cartItem.setDiscount(product.getDiscount());
			cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * quantity));
			cartRepository.save(cart);
			cartItemRepository.save(cartItem);
		}

		CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

		List<CartItem> cartItems = cart.getCartItems();

		Stream<ProductDTO> productStream = cartItems.stream().map(item -> {
			ProductDTO prd = modelMapper.map(item.getProduct(), ProductDTO.class);
			prd.setQuantity(item.getQuantity());
			return prd;
		});

		cartDTO.setProduct(productStream.toList());
		return cartDTO;
	}

	private Cart createCart() {
		Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
		if (userCart != null) {
			return userCart;
		}

		Cart cart = new Cart();
		cart.setTotalPrice(0.00);
		cart.setUser(authUtil.loggedInUser());

		return cartRepository.save(cart);
	}

	@Transactional
	@Override
	public String deleteProductFromCart(Long cartId, Long productId) {

	    Cart cart = cartRepository.findById(cartId)
	            .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

	    CartItem cartItem = cartItemRepository.findByProduct_ProductIdAndCart_CartId(productId, cartId);

	    if (cartItem == null) {
	        throw new ResourceNotFoundException("Product", "productId", productId);
	    }

	    
	    cart.setTotalPrice(cart.getTotalPrice() - 
	        (cartItem.getProductPrice() * cartItem.getQuantity()));

	    
	    cart.getCartItems().remove(cartItem);

	   
	    cartItemRepository.delete(cartItem);

	  
	    cartRepository.save(cart);

	    return "Product " + cartItem.getProduct().getProductName() + " removed from the cart !!!";
	}

	@Override
	public void updateProductInCarts(Long cartId, Long productId) {
		Cart cart = cartRepository.findById(cartId)
				.orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

		CartItem cartItem = cartItemRepository.findByProduct_ProductIdAndCart_CartId(productId, cartId);

		if (cartItem == null) {
			throw new APIException("Product " + product.getProductName() + " not available in the cart!!!");
		}

		double cartPrice = cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity());

		cartItem.setProductPrice(product.getSpecialPrice());

		cart.setTotalPrice(cartPrice + (cartItem.getProductPrice() * cartItem.getQuantity()));

		cartItemRepository.save(cartItem);
		cartRepository.save(cart);
	}
}
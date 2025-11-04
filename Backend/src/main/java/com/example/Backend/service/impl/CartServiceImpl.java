package com.example.Backend.service.impl;

import com.example.Backend.entity.Cart;
import com.example.Backend.entity.CartItem;
import com.example.Backend.entity.Product;
import com.example.Backend.entity.User;
import com.example.Backend.repository.CartItemRepository;
import com.example.Backend.repository.CartRepository;
import com.example.Backend.repository.ProductRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.CartService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public Cart getOrCreateCart(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Cart cart = user.getCart();
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart = cartRepository.save(cart);
            user.setCart(cart);
            userRepository.save(user);
        }
        return cart;
    }

    @Override
    @Transactional(readOnly = true)
    public Cart getCartById(UUID cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found with id: " + cartId));
    }

    @Override
    public Cart addItemToCart(UUID userId, UUID productId, Integer quantity) {
        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));

        if (!product.isInStock()) {
            throw new IllegalStateException("Product is out of stock");
        }

        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock");
        }

        // Check if item already exists in cart
        CartItem existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cartItemRepository.save(newItem);
        }

        return getCartById(cart.getId());
    }

    @Override
    public Cart updateCartItemQuantity(UUID userId, UUID productId, Integer quantity) {
        Cart cart = getOrCreateCart(userId);
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found"));

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Product product = cartItem.getProduct();
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock");
        }

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        return getCartById(cart.getId());
    }

    @Override
    public Cart removeItemFromCart(UUID userId, UUID productId) {
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);
        return getCartById(cart.getId());
    }

    @Override
    public Cart clearCart(UUID userId) {
        Cart cart = getOrCreateCart(userId);
        cart.clearCart();
        cartRepository.save(cart);
        return cart;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCartTotalPrice(UUID userId) {
        Cart cart = getOrCreateCart(userId);
        return cart.getTotalPrice();
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getCartTotalItems(UUID userId) {
        Cart cart = getOrCreateCart(userId);
        return cart.getTotalItems();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateCart(UUID userId) {
        Cart cart = getOrCreateCart(userId);

        for (CartItem item : cart.getCartItems()) {
            Product product = item.getProduct();
            if (!product.isInStock() || product.getStock() < item.getQuantity()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Cart mergeGuestCart(UUID userId, UUID guestCartId) {
        Cart userCart = getOrCreateCart(userId);
        Cart guestCart = getCartById(guestCartId);

        for (CartItem guestItem : guestCart.getCartItems()) {
            CartItem existingItem = cartItemRepository.findByCartIdAndProductId(
                    userCart.getId(), guestItem.getProduct().getId()).orElse(null);

            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + guestItem.getQuantity());
                cartItemRepository.save(existingItem);
            } else {
                CartItem newItem = new CartItem();
                newItem.setCart(userCart);
                newItem.setProduct(guestItem.getProduct());
                newItem.setQuantity(guestItem.getQuantity());
                cartItemRepository.save(newItem);
            }
        }

        // Delete guest cart
        cartRepository.delete(guestCart);

        return getCartById(userCart.getId());
    }
}

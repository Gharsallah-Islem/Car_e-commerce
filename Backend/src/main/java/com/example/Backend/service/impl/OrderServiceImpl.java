package com.example.Backend.service.impl;

import com.example.Backend.dto.OrderDTO;
import com.example.Backend.entity.*;
import com.example.Backend.repository.*;
import com.example.Backend.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @Override
    public Order createOrderFromCart(UUID userId, OrderDTO orderDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Cart cart = user.getCart();
        if (cart == null || cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        // Validate cart items stock
        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();
            if (!product.isInStock()) {
                throw new IllegalStateException("Product " + product.getName() + " is out of stock");
            }
            if (product.getStock() < cartItem.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
            }
        }

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setStatus("PENDING");
        order.setDeliveryAddress(orderDTO.getShippingAddress());
        order.setTotalPrice(cart.getTotalPrice());
        order.setPaymentStatus("PENDING");
        order.setPaymentMethod(orderDTO.getPaymentMethod());
        order.setNotes(orderDTO.getNotes());

        Order savedOrder = orderRepository.save(order);

        // Create order items and update product stock
        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());
            orderItemRepository.save(orderItem);

            // Decrease product stock
            Product product = cartItem.getProduct();
            product.decreaseStock(cartItem.getQuantity());
            productRepository.save(product);
        }

        // Clear cart
        cart.clearCart();
        cartRepository.save(cart);

        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getOrdersByUser(UUID userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getOrdersByStatus(String status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getPendingOrders(Pageable pageable) {
        return orderRepository.findByStatus("PENDING", pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getOrdersForDashboard(String status, LocalDateTime startDate,
            LocalDateTime endDate, Pageable pageable) {
        return orderRepository.findOrdersForDashboard(status, startDate, endDate, pageable);
    }

    @Override
    public Order updateOrderStatus(UUID orderId, String status) {
        Order order = getOrderById(orderId);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Override
    public Order confirmOrder(UUID orderId) {
        Order order = getOrderById(orderId);
        if (!"PENDING".equals(order.getStatus())) {
            throw new IllegalStateException("Only pending orders can be confirmed");
        }
        order.setStatus("CONFIRMED");
        return orderRepository.save(order);
    }

    @Override
    public Order shipOrder(UUID orderId, String trackingNumber) {
        Order order = getOrderById(orderId);
        if (!"CONFIRMED".equals(order.getStatus()) && !"PENDING".equals(order.getStatus())) {
            throw new IllegalStateException("Order must be confirmed before shipping");
        }
        order.setStatus("SHIPPED");
        order.setTrackingNumber(trackingNumber);
        return orderRepository.save(order);
    }

    @Override
    public Order markAsDelivered(UUID orderId) {
        Order order = getOrderById(orderId);
        if (!"SHIPPED".equals(order.getStatus())) {
            throw new IllegalStateException("Order must be shipped before marking as delivered");
        }
        order.markAsDelivered();
        return orderRepository.save(order);
    }

    @Override
    public Order cancelOrder(UUID orderId, String reason) {
        Order order = getOrderById(orderId);
        if ("DELIVERED".equals(order.getStatus())) {
            throw new IllegalStateException("Cannot cancel delivered order");
        }

        // Restore product stock
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.increaseStock(item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus("CANCELLED");
        order.setNotes((order.getNotes() != null ? order.getNotes() + "\n" : "") + "Cancelled: " + reason);
        return orderRepository.save(order);
    }

    @Override
    public Order processPayment(UUID orderId, String paymentIntentId) {
        Order order = getOrderById(orderId);
        order.setPaymentStatus("COMPLETED");
        order.setNotes((order.getNotes() != null ? order.getNotes() + "\n" : "") + "Payment ID: " + paymentIntentId);
        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalRevenue() {
        return orderRepository.calculateTotalRevenue();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateRevenueBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.calculateRevenueBetween(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getOrderStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", orderRepository.count());
        stats.put("pending", orderRepository.countByStatus("PENDING"));
        stats.put("confirmed", orderRepository.countByStatus("CONFIRMED"));
        stats.put("shipped", orderRepository.countByStatus("SHIPPED"));
        stats.put("delivered", orderRepository.countByStatus("DELIVERED"));
        stats.put("cancelled", orderRepository.countByStatus("CANCELLED"));
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Long countOrdersByUser(UUID userId) {
        return orderRepository.countByUserId(userId);
    }
}

package com.example.Backend.entity;

/**
 * Enum for different types of admin notifications
 */
public enum AdminNotificationType {
    NEW_ORDER, // New order placed
    ORDER_CANCELLED, // Order cancelled
    LOW_STOCK, // Product stock below threshold
    OUT_OF_STOCK, // Product out of stock
    NEW_USER, // New user registered
    DELIVERY_COMPLETED, // Delivery completed successfully
    DELIVERY_FAILED, // Delivery failed
    PAYMENT_RECEIVED, // Payment received
    SYSTEM_ALERT // System alerts
}

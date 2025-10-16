package com.example.Backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    private String billingAddress;

    private String phoneNumber;

    private String notes;

    private String paymentMethod;

    private Map<String, Object> deliveryPreferences;
}

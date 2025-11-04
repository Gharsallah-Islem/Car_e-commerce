package com.example.Backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryDTO {

    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;

    private String recipientName;

    private String recipientPhone;

    private String courierName; // ONdelivery courier

    private Map<String, Object> ondeliveryData; // ONdelivery API integration data
}

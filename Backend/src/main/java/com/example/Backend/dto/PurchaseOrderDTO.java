package com.example.Backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderDTO {
    @NotNull(message = "Supplier ID is required")
    private UUID supplierId;
    
    private LocalDate orderDate;
    private LocalDate expectedDelivery;
    private String notes;
    private String status; // DRAFT, PENDING, APPROVED, RECEIVED, CANCELLED
    
    private List<PurchaseOrderItemDTO> items;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseOrderItemDTO {
        @NotNull(message = "Product ID is required")
        private UUID productId;
        
        @NotNull(message = "Quantity is required")
        private Integer quantity;
        
        @NotNull(message = "Unit price is required")
        private Double unitPrice;
    }
}

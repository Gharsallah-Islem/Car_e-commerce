package com.example.Backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderSettingDTO {
    @NotNull(message = "Product ID is required")
    private UUID productId;
    
    @NotNull(message = "Reorder point is required")
    @Positive(message = "Reorder point must be positive")
    private Integer reorderPoint;
    
    @NotNull(message = "Reorder quantity is required")
    @Positive(message = "Reorder quantity must be positive")
    private Integer reorderQuantity;
    
    private UUID preferredSupplierId;
    private Boolean autoReorder;
}

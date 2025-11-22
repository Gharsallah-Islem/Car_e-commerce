package com.example.Backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDTO {
    @NotBlank(message = "Supplier name is required")
    private String name;
    
    private String contactPerson;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String phone;
    private String address;
    private String status; // ACTIVE, INACTIVE
    private Boolean isActive;
    private Double rating;
}

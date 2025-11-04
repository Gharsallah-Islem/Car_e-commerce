package com.example.Backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {

    @NotBlank(message = "Report type is required")
    private String reportType; // SALES, INVENTORY, USERS, ORDERS, ANALYTICS

    private String title;

    private String description;

    private String startDate; // ISO format date string

    private String endDate; // ISO format date string

    private String format; // JSON, CSV, PDF
}

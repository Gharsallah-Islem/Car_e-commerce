package com.example.Backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reports", indexes = {
        @Index(name = "idx_reports_report_type", columnList = "report_type"),
        @Index(name = "idx_reports_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NotBlank(message = "Report type is required")
    @Column(name = "report_type", nullable = false, length = 100)
    private String reportType; // SALES, INVENTORY, USERS, ORDERS, ANALYTICS

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "data", columnDefinition = "TEXT")
    private String data; // JSON formatted report data

    @Column(name = "file_url")
    private String fileUrl; // CSV/PDF export URL

    @Column(name = "generated_by", columnDefinition = "UUID")
    private UUID generatedBy; // Admin who generated the report

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Report type constants
    public static final String TYPE_SALES = "SALES";
    public static final String TYPE_INVENTORY = "INVENTORY";
    public static final String TYPE_USERS = "USERS";
    public static final String TYPE_ORDERS = "ORDERS";
    public static final String TYPE_ANALYTICS = "ANALYTICS";
}

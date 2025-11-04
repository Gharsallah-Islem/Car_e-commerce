package com.example.Backend.service;

import com.example.Backend.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ReportService {

    /**
     * Generate sales report
     * 
     * @param startDate Start date
     * @param endDate   End date
     * @return Generated report
     */
    Report generateSalesReport(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Generate product performance report
     * 
     * @param startDate Start date
     * @param endDate   End date
     * @return Generated report
     */
    Report generateProductReport(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Generate user activity report
     * 
     * @param startDate Start date
     * @param endDate   End date
     * @return Generated report
     */
    Report generateUserReport(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Generate custom report
     * 
     * @param reportType Report type
     * @param startDate  Start date
     * @param endDate    End date
     * @param parameters Additional parameters
     * @return Generated report
     */
    Report generateCustomReport(String reportType, LocalDateTime startDate,
            LocalDateTime endDate, Map<String, Object> parameters);

    /**
     * Get report by ID
     * 
     * @param reportId Report ID
     * @return Report entity
     */
    Report getReportById(UUID reportId);

    /**
     * Get all reports
     * 
     * @param pageable Pagination parameters
     * @return Page of reports
     */
    Page<Report> getAllReports(Pageable pageable);

    /**
     * Get reports by type
     * 
     * @param reportType Report type
     * @param pageable   Pagination parameters
     * @return Page of reports
     */
    Page<Report> getReportsByType(String reportType, Pageable pageable);

    /**
     * Get reports created by admin
     * 
     * @param adminId  Admin ID
     * @param pageable Pagination parameters
     * @return Page of reports
     */
    Page<Report> getReportsByCreator(UUID adminId, Pageable pageable);

    /**
     * Get recent reports
     * 
     * @param limit Number of reports
     * @return List of recent reports
     */
    List<Report> getRecentReports(int limit);

    /**
     * Delete report
     * 
     * @param reportId Report ID
     */
    void deleteReport(UUID reportId);

    /**
     * Get dashboard statistics
     * 
     * @return Map of key statistics
     */
    Map<String, Object> getDashboardStatistics();
}

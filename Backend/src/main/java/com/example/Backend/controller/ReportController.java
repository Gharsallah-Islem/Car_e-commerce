package com.example.Backend.controller;

import com.example.Backend.entity.Report;
import com.example.Backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.Backend.security.UserPrincipal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class ReportController {

    private final ReportService reportService;

    /**
     * Generate sales report
     * POST /api/reports/sales
     */
    @PostMapping("/sales")
    public ResponseEntity<Report> generateSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Report report = reportService.generateSalesReport(startDate, endDate);
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    /**
     * Generate product performance report
     * POST /api/reports/products
     */
    @PostMapping("/products")
    public ResponseEntity<Report> generateProductReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Report report = reportService.generateProductReport(startDate, endDate);
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    /**
     * Generate user activity report
     * POST /api/reports/users
     */
    @PostMapping("/users")
    public ResponseEntity<Report> generateUserReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Report report = reportService.generateUserReport(startDate, endDate);
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    /**
     * Generate custom report
     * POST /api/reports/custom
     */
    @PostMapping("/custom")
    public ResponseEntity<Report> generateCustomReport(
            @RequestParam String reportType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestBody(required = false) Map<String, Object> parameters) {

        Report report = reportService.generateCustomReport(reportType, startDate, endDate, parameters);
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    /**
     * Get report by ID
     * GET /api/reports/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Report> getReport(@PathVariable UUID id) {
        Report report = reportService.getReportById(id);
        return ResponseEntity.ok(report);
    }

    /**
     * Get all reports
     * GET /api/reports
     */
    @GetMapping
    public ResponseEntity<Page<Report>> getAllReports(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<Report> reports = reportService.getAllReports(pageable);
        return ResponseEntity.ok(reports);
    }

    /**
     * Get reports by type
     * GET /api/reports/type/{reportType}
     */
    @GetMapping("/type/{reportType}")
    public ResponseEntity<Page<Report>> getReportsByType(
            @PathVariable String reportType,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<Report> reports = reportService.getReportsByType(reportType, pageable);
        return ResponseEntity.ok(reports);
    }

    /**
     * Get reports created by me
     * GET /api/reports/my-reports
     */
    @GetMapping("/my-reports")
    public ResponseEntity<Page<Report>> getMyReports(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Page<Report> reports = reportService.getReportsByCreator(currentUser.getId(), pageable);
        return ResponseEntity.ok(reports);
    }

    /**
     * Get reports by creator (SUPER_ADMIN only)
     * GET /api/reports/creator/{creatorId}
     */
    @GetMapping("/creator/{creatorId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<Report>> getReportsByCreator(
            @PathVariable UUID creatorId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<Report> reports = reportService.getReportsByCreator(creatorId, pageable);
        return ResponseEntity.ok(reports);
    }

    /**
     * Get recent reports
     * GET /api/reports/recent
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Report>> getRecentReports(
            @RequestParam(defaultValue = "10") int limit) {

        List<Report> reports = reportService.getRecentReports(limit);
        return ResponseEntity.ok(reports);
    }

    /**
     * Delete report (SUPER_ADMIN only)
     * DELETE /api/reports/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteReport(@PathVariable UUID id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get dashboard statistics
     * GET /api/reports/dashboard/statistics
     */
    @GetMapping("/dashboard/statistics")
    public ResponseEntity<Map<String, Object>> getDashboardStatistics() {
        Map<String, Object> statistics = reportService.getDashboardStatistics();
        return ResponseEntity.ok(statistics);
    }
}


package com.aicarsales.app.admin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "csv_upload_jobs")
public class CsvUploadJob {

    @Id
    private UUID id;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CsvUploadJobStatus status;

    private String message;

    @Column(name = "error_report")
    private String errorReport;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    protected CsvUploadJob() {
    }

    public CsvUploadJob(UUID id, String originalFilename, CsvUploadJobStatus status) {
        this.id = id;
        this.originalFilename = originalFilename;
        this.status = status;
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public CsvUploadJobStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorReport() {
        return errorReport;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public void markProcessing() {
        this.status = CsvUploadJobStatus.PROCESSING;
        this.message = "Processing";
        this.errorReport = null;
        this.completedAt = null;
    }

    public void markSuccess(String message) {
        this.status = CsvUploadJobStatus.SUCCEEDED;
        this.message = message;
        this.completedAt = OffsetDateTime.now();
    }

    public void markFailure(String message, String errorReport) {
        this.status = CsvUploadJobStatus.FAILED;
        this.message = message;
        this.errorReport = errorReport;
        this.completedAt = OffsetDateTime.now();
    }
}

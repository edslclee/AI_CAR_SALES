
package com.aicarsales.app.admin.dto;

import java.time.OffsetDateTime;

public record UploadStatusResponse(
        String jobId,
        String status,
        String message,
        String errorReport,
        OffsetDateTime completedAt
) {
}

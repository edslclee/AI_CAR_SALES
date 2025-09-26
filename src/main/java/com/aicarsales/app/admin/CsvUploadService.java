
package com.aicarsales.app.admin;

import com.aicarsales.app.admin.dto.CsvUploadRequest;
import com.aicarsales.app.admin.dto.UploadStatusResponse;
import java.util.Optional;

public interface CsvUploadService {
    String enqueueUpload(CsvUploadRequest request);
    Optional<UploadStatusResponse> getStatus(String jobId);
}


package com.aicarsales.app.admin;

import com.aicarsales.app.admin.dto.CsvUploadRequest;
import com.aicarsales.app.admin.dto.CsvUploadResponse;
import com.aicarsales.app.admin.dto.UploadStatusResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/cars")
@Validated
public class AdminCsvUploadController {

    private final CsvUploadService csvUploadService;

    public AdminCsvUploadController(CsvUploadService csvUploadService) {
        this.csvUploadService = csvUploadService;
    }

    @PostMapping("/upload")
    public ResponseEntity<CsvUploadResponse> upload(@RequestParam("file") @NotNull MultipartFile file) {
        String jobId = csvUploadService.enqueueUpload(new CsvUploadRequest(file));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new CsvUploadResponse(jobId));
    }

    @GetMapping("/upload/{jobId}")
    public ResponseEntity<UploadStatusResponse> getStatus(@PathVariable String jobId) {
        return csvUploadService.getStatus(jobId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}

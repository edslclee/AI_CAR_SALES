
package com.aicarsales.app.admin.dto;

import org.springframework.web.multipart.MultipartFile;

public record CsvUploadRequest(MultipartFile file) {
}

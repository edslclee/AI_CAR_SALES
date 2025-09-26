
package com.aicarsales.app.admin;

import com.aicarsales.app.admin.dto.CsvUploadRequest;
import com.aicarsales.app.admin.dto.UploadStatusResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CsvUploadServiceImpl implements CsvUploadService {

    private final CsvUploadJobRepository jobRepository;
    private final CsvValidator csvValidator;
    private final CarImportProcessor carImportProcessor;
    private final Executor csvUploadTaskExecutor;

    public CsvUploadServiceImpl(CsvUploadJobRepository jobRepository,
                                 CsvValidator csvValidator,
                                 CarImportProcessor carImportProcessor,
                                 @org.springframework.beans.factory.annotation.Qualifier("csvUploadTaskExecutor") Executor csvUploadTaskExecutor) {
        this.jobRepository = jobRepository;
        this.csvValidator = csvValidator;
        this.carImportProcessor = carImportProcessor;
        this.csvUploadTaskExecutor = csvUploadTaskExecutor;
    }

    @Override
    @Transactional
    public String enqueueUpload(CsvUploadRequest request) {
        MultipartFile file = request.file();
        if (file == null || file.isEmpty()) {
            throw new CsvValidationException("업로드 파일이 비어 있습니다.");
        }
        if (file.getContentType() != null && !file.getContentType().contains("csv")
                && !MediaType.TEXT_PLAIN_VALUE.equals(file.getContentType())) {
            throw new CsvValidationException("CSV 파일만 업로드할 수 있습니다.");
        }

        byte[] payload;
        try {
            payload = file.getBytes();
        } catch (IOException e) {
            throw new CsvValidationException("파일을 읽는 중 오류가 발생했습니다.");
        }

        List<CarCsvRecord> records = csvValidator.parse(payload, file.getOriginalFilename() == null ? "upload.csv" : file.getOriginalFilename());

        UUID jobId = UUID.randomUUID();
        CsvUploadJob job = new CsvUploadJob(jobId, file.getOriginalFilename() == null ? "unknown.csv" : file.getOriginalFilename(), CsvUploadJobStatus.PENDING);
        jobRepository.save(job);

        csvUploadTaskExecutor.execute(() -> process(jobId, records));

        return jobId.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UploadStatusResponse> getStatus(String jobId) {
        try {
            UUID uuid = UUID.fromString(jobId);
            return jobRepository.findById(uuid).map(job -> new UploadStatusResponse(
                    job.getId().toString(),
                    job.getStatus().name(),
                    job.getMessage(),
                    job.getErrorReport(),
                    job.getCompletedAt()
            ));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Transactional
    void process(UUID jobId, List<CarCsvRecord> records) {
        CsvUploadJob job = jobRepository.findById(jobId).orElseThrow();
        job.markProcessing();
        jobRepository.save(job);
        try {
            var result = carImportProcessor.importRecords(records);
            job.markSuccess(result.asMessage());
        } catch (CsvValidationException e) {
            job.markFailure("검증 오류", e.getMessage());
        } catch (Exception e) {
            job.markFailure("업로드 처리 중 오류 발생", e.getMessage());
        }
        jobRepository.save(job);
    }
}

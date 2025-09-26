
package com.aicarsales.app.admin;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CsvUploadJobRepository extends JpaRepository<CsvUploadJob, UUID> {
    Optional<CsvUploadJob> findById(UUID id);
}

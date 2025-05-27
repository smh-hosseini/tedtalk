package com.iodigital.assignment.tedtalks.importcsv.repository;

import com.iodigital.assignment.tedtalks.importcsv.model.ImportJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImportJobRepository extends JpaRepository<ImportJob, Long> {

    Optional<ImportJob> findByFileHash(String fileHash);

}

package com.iodigital.assignment.tedtalks.talk.service;

import com.iodigital.assignment.tedtalks.importcsv.exception.TedTalkImportException;
import com.iodigital.assignment.tedtalks.importcsv.event.FileUploadEvent;
import com.iodigital.assignment.tedtalks.importcsv.model.ImportJob;
import com.iodigital.assignment.tedtalks.importcsv.repository.ImportJobRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static com.iodigital.assignment.tedtalks.common.io.FileUtils.calculateStreamHash;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportJobServiceImpl implements ImportJobService {

    private static final String FILE_ALREADY_PROCESSED_MESSAGE = "File has already been processed, skipping import job creation.";

    @Value("${tedtalks.upload.dir}")
    private String uploadDir;

    private final ImportJobRepository importJobRepository;
    private final ApplicationEventPublisher eventPublisher;

    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            log.error("Failed to initialize upload directory: {}", uploadDir, e);
            throw new TedTalkImportException("Failed to initialize upload directory", e);
        }
    }

    @Override
    public ImportJob createImportJob(MultipartFile file) {
        try {
            final var fileName = file.getOriginalFilename();
            final var fileHash = calculateStreamHash(file.getInputStream());
            if (isFileAlreadyProcessed(fileHash)) {
                log.info(FILE_ALREADY_PROCESSED_MESSAGE);
                return importJobRepository.findByFileHash(fileHash).orElse(null);
            }
            Path uploadPath = Paths.get(uploadDir);
            // Save the file with original filename
            assert fileName != null;
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return createAndPublishJob(fileName, filePath.toString(), fileHash);
        } catch (Exception e) {
            log.error("Error creating import job for file: {}", file.getOriginalFilename(), e);
            return null;
        }

    }

    @Override
    @Transactional
    public void createImportJobForClassPathResource(Resource resource) {
        if (resource == null || !resource.exists()) {
            throw new IllegalArgumentException("Resource not found: " + resource);
        }
        try {
            String fileName = resource.getFilename();
            String fileHash = calculateStreamHash(resource.getInputStream());
            if (isFileAlreadyProcessed(fileHash)) {
                log.info(FILE_ALREADY_PROCESSED_MESSAGE);
                return;
            }
            assert fileName != null;
            createAndPublishJob(fileName, resource.getFile().getAbsolutePath(), fileHash);
        } catch (IOException e) {
            log.error("Error creating import job for resource: {}", resource.getFilename(), e);
            throw new TedTalkImportException("Failed to create import job for resource", e);
        }
    }

    private boolean isFileAlreadyProcessed(String fileHash) {
        return importJobRepository.findByFileHash(fileHash).isPresent();
    }

    private ImportJob createAndPublishJob(String fileName, String filePath, String fileHash) {
        ImportJob job = ImportJob.builder()
                .fileName(fileName)
                .filePath(filePath)
                .fileHash(fileHash)
                .status(ImportJob.Status.PENDING)
                .build();

        ImportJob savedJob = importJobRepository.save(job);
        eventPublisher.publishEvent(new FileUploadEvent(savedJob));
        return savedJob;
    }
}

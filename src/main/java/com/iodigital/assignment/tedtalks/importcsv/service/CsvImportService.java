package com.iodigital.assignment.tedtalks.importcsv.service;

import com.iodigital.assignment.tedtalks.common.io.FileSystemResourceProvider;
import com.iodigital.assignment.tedtalks.importcsv.event.FileUploadEvent;
import com.iodigital.assignment.tedtalks.importcsv.model.ImportJob;
import com.iodigital.assignment.tedtalks.importcsv.model.ImportJob.Status;
import com.iodigital.assignment.tedtalks.importcsv.reader.TedTalkCSVReader;
import com.iodigital.assignment.tedtalks.importcsv.reader.CsvReaderFactory;
import com.iodigital.assignment.tedtalks.importcsv.repository.ImportJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvImportService {

    private final ImportJobRepository importJobRepository;
    private final FileSystemResourceProvider fileResourceProvider;
    private final CsvReaderFactory csvReaderFactory;
    private final CsvProcessingService csvProcessingService;

    @EventListener
    public void handleFileUploadedEvent(FileUploadEvent event) {
        log.info("Processing newly uploaded file: {}", event.importJob().getFileName());
        startImportJob(event.importJob());
    }

    public void startImportJob(ImportJob importJob) {
        startImport(importJob);
    }


    private void startImport(ImportJob job) {
        try (InputStream inputStream = fileResourceProvider.getInputStream(job.getFilePath());
             TedTalkCSVReader csvReader = csvReaderFactory.createTedTalkReader(inputStream)) {
            csvProcessingService.processImportJob(job, csvReader);
            completeImportJob(job);

        } catch (Exception e) {
            failImportJob(job, e);
        }
    }

    private void completeImportJob(ImportJob job) {
        job.setStatus(Status.COMPLETED);
        importJobRepository.save(job);
        log.info("Import job completed successfully: {}", job.getFileName());
    }

    private void failImportJob(ImportJob job, Exception e) {
        job.setStatus(Status.FAILED);
        importJobRepository.save(job);
        log.error("Failed to process CSV file {} - cause: {}", job.getFileName(), e.getMessage());
    }
}

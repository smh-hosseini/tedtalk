package com.iodigital.assignment.tedtalks.infra;

import com.iodigital.assignment.tedtalks.importcsv.exception.TedTalkImportException;
import com.iodigital.assignment.tedtalks.common.io.FileSystemResourceProvider;
import com.iodigital.assignment.tedtalks.importcsv.model.ImportJob;
import com.iodigital.assignment.tedtalks.importcsv.model.ImportJob.Status;
import com.iodigital.assignment.tedtalks.importcsv.service.CsvProcessingService;
import com.iodigital.assignment.tedtalks.importcsv.reader.TedTalkCSVReader;
import com.iodigital.assignment.tedtalks.importcsv.reader.CsvReaderFactory;
import com.iodigital.assignment.tedtalks.importcsv.repository.ImportJobRepository;
import com.iodigital.assignment.tedtalks.importcsv.service.CsvImportService;
import com.opencsv.exceptions.CsvValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CsvImportServiceTest {

    @Mock private ImportJobRepository importJobRepository;
    @Mock private FileSystemResourceProvider fileResourceProvider;
    @Mock private CsvReaderFactory csvReaderFactory;
    @Mock private CsvProcessingService csvProcessingService;
    @Mock private TedTalkCSVReader csvReader;
    @Mock private InputStream inputStream;

    @InjectMocks
    private CsvImportService csvImportService;

    @Test
    void shouldCompleteImportSuccessfully() throws IOException, CsvValidationException {
        // Given
        ImportJob job = createImportJob("/test/file.csv");

        when(fileResourceProvider.getInputStream("/test/file.csv")).thenReturn(inputStream);
        when(csvReaderFactory.createTedTalkReader(inputStream)).thenReturn(csvReader);

        // When
        csvImportService.startImportJob(job);

        // Then
        verify(csvProcessingService).processImportJob(job, csvReader);
        verify(importJobRepository).save(job);
        assertEquals(Status.COMPLETED, job.getStatus());
        verify(inputStream).close(); // Verify resource cleanup
    }

    @Test
    void shouldFailImportOnFileAccessError() throws IOException, CsvValidationException {
        // Given
        ImportJob job = createImportJob("/invalid/file.csv");

        when(fileResourceProvider.getInputStream("/invalid/file.csv"))
                .thenThrow(new IOException("File not found"));

        // When
        csvImportService.startImportJob(job);

        // Then
        verify(csvProcessingService, never()).processImportJob(any(), any());
        verify(importJobRepository).save(job);
        assertEquals(Status.FAILED, job.getStatus());
    }

    @Test
    void shouldFailImportOnCsvReaderCreationError() throws IOException, CsvValidationException {
        // Given
        ImportJob job = createImportJob("/test/corrupted.csv");

        when(fileResourceProvider.getInputStream("/test/corrupted.csv")).thenReturn(inputStream);
        when(csvReaderFactory.createTedTalkReader(inputStream))
                .thenThrow(new TedTalkImportException("Invalid CSV format"));

        // When
        csvImportService.startImportJob(job);

        // Then
        verify(csvProcessingService, never()).processImportJob(any(), any());
        verify(importJobRepository).save(job);
        assertEquals(Status.FAILED, job.getStatus());
    }

    @Test
    void shouldFailImportOnProcessingError() throws IOException, CsvValidationException {
        // Given
        ImportJob job = createImportJob("/test/file.csv");

        when(fileResourceProvider.getInputStream("/test/file.csv")).thenReturn(inputStream);
        when(csvReaderFactory.createTedTalkReader(inputStream)).thenReturn(csvReader);
        doThrow(new RuntimeException("Database connection failed"))
                .when(csvProcessingService).processImportJob(job, csvReader);

        // When
        csvImportService.startImportJob(job);

        // Then
        verify(importJobRepository).save(job);
        assertEquals(Status.FAILED, job.getStatus());
    }

    @Test
    void shouldEnsureResourceCleanupOnException() throws IOException, CsvValidationException {
        // Given
        ImportJob job = createImportJob("/test/file.csv");

        when(fileResourceProvider.getInputStream("/test/file.csv")).thenReturn(inputStream);
        when(csvReaderFactory.createTedTalkReader(inputStream)).thenReturn(csvReader);
        doThrow(new RuntimeException("Unexpected error"))
                .when(csvProcessingService).processImportJob(job, csvReader);

        // When
        csvImportService.startImportJob(job);

        // Then
        verify(inputStream).close(); // Resources should still be cleaned up
        verify(csvReader).close();
        assertEquals(Status.FAILED, job.getStatus());
    }

    private ImportJob createImportJob(String filePath) {
        ImportJob job = new ImportJob();
        job.setFilePath(filePath);
        job.setFileName(filePath.substring(filePath.lastIndexOf('/') + 1));
        job.setStatus(Status.PENDING);
        return job;
    }
}
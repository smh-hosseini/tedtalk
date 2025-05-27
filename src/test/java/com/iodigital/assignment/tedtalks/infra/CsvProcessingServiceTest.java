package com.iodigital.assignment.tedtalks.infra;

import com.iodigital.assignment.tedtalks.importcsv.model.ImportJob;
import com.iodigital.assignment.tedtalks.talk.model.TedTalk;
import com.iodigital.assignment.tedtalks.common.config.CsvImportProperties;
import com.iodigital.assignment.tedtalks.importcsv.service.CsvProcessingServiceImpl;
import com.iodigital.assignment.tedtalks.importcsv.reader.TedTalkCSVReader;
import com.iodigital.assignment.tedtalks.importcsv.model.TedTalkRecord;
import com.iodigital.assignment.tedtalks.importcsv.repository.ImportJobRepository;
import com.iodigital.assignment.tedtalks.talk.repository.TedTalkRepository;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CsvProcessingServiceTest {

    @Mock
    private ImportJobRepository importJobRepository;
    @Mock
    private TedTalkRepository tedTalkRepository;
    @Mock
    private Validator validator;
    @Mock
    private CsvImportProperties csvImportProperties;
    @Mock
    private TedTalkCSVReader csvReader;

    @InjectMocks
    private CsvProcessingServiceImpl csvProcessingService;

    @Test
    void shouldProcessBatchSuccessfully() {
        // Given
        ImportJob job = new ImportJob();
        TedTalkRecord record = createValidTedTalkRecord();
        List<TedTalkRecord> batch = List.of(record);

        when(validator.validate(any(TedTalk.class))).thenReturn(Set.of());
        when(tedTalkRepository.findByTitleAndSpeakerAndDate(any(), any(), any()))
                .thenReturn(Optional.empty());

        // When
        csvProcessingService.processBatch(job, batch);

        // Then
        verify(tedTalkRepository).save(any(TedTalk.class));
        assertEquals(1, job.getSuccessfulCount());
    }

    @Test
    void shouldSkipDuplicateRecords() {
        // Given
        ImportJob job = new ImportJob();
        TedTalkRecord record = createValidTedTalkRecord();
        List<TedTalkRecord> batch = List.of(record);

        when(validator.validate(any(TedTalk.class))).thenReturn(Set.of());
        when(tedTalkRepository.findByTitleAndSpeakerAndDate(any(), any(), any()))
                .thenReturn(Optional.of(new TedTalk()));

        // When
        csvProcessingService.processBatch(job, batch);

        // Then
        verify(tedTalkRepository, never()).save(any(TedTalk.class));
        assertEquals(1, job.getSkippedCount());
    }

    @Test
    void shouldHandleValidationErrors() {
        // Given
        ImportJob job = new ImportJob();
        TedTalkRecord record = createInvalidTedTalkRecord();

        // When
        csvProcessingService.processRecord(job, record);

        // Then
        verify(tedTalkRepository, never()).save(any(TedTalk.class));
        assertEquals(1, job.getFailedCount());
    }

    @Test
    void shouldProcessSingleRecordSuccessfully() {
        // Given
        ImportJob job = new ImportJob();
        TedTalkRecord record = createValidTedTalkRecord();

        when(validator.validate(any(TedTalk.class))).thenReturn(Set.of());
        when(tedTalkRepository.findByTitleAndSpeakerAndDate(any(), any(), any()))
                .thenReturn(Optional.empty());

        // When
        csvProcessingService.processRecord(job, record);

        // Then
        verify(tedTalkRepository).save(any(TedTalk.class));
        assertEquals(1, job.getSuccessfulCount());
    }

    @Test
    void shouldProcessImportJobWithMultipleBatches() throws IOException, CsvValidationException {
        // Given
        ImportJob job = new ImportJob();
        job.setLastProcessedLine(0);

        List<TedTalkRecord> batch1 = List.of(createValidTedTalkRecord());
        List<TedTalkRecord> batch2 = List.of(createValidTedTalkRecord());
        List<TedTalkRecord> emptyBatch = List.of();

        when(csvImportProperties.getBatchSize()).thenReturn(1);
        when(csvReader.readBatch(1))
                .thenReturn(batch1)
                .thenReturn(batch2)
                .thenReturn(emptyBatch);

        when(validator.validate(any(TedTalk.class))).thenReturn(Set.of());
        when(tedTalkRepository.findByTitleAndSpeakerAndDate(any(), any(), any()))
                .thenReturn(Optional.empty());

        // When
        csvProcessingService.processImportJob(job, csvReader);

        // Then
        verify(importJobRepository, times(3)).save(job);
    }

    @Test
    void shouldResumeFromLastProcessedLine() throws IOException, CsvValidationException {
        // Given
        ImportJob job = new ImportJob();
        job.setLastProcessedLine(100);

        when(csvReader.readBatch(anyInt())).thenReturn(List.of());

        // When
        csvProcessingService.processImportJob(job, csvReader);

        // Then
        verify(csvReader).skip(100);
    }

    @Test
    void shouldProcessMixedBatchWithValidAndInvalidRecords() {
        // Given
        ImportJob job = new ImportJob();
        TedTalkRecord validRecord = createValidTedTalkRecord();
        TedTalkRecord invalidRecord = createInvalidTedTalkRecord();
        List<TedTalkRecord> batch = List.of(validRecord, invalidRecord);

        // Mock validation: first record valid, second invalid
        when(validator.validate(any(TedTalk.class)))
                .thenReturn(Set.of()) // Valid
                .thenReturn(Set.of(mock(ConstraintViolation.class))); // Invalid

        when(tedTalkRepository.findByTitleAndSpeakerAndDate(any(), any(), any()))
                .thenReturn(Optional.empty());

        // When
        csvProcessingService.processBatch(job, batch);

        // Then
        verify(tedTalkRepository, times(1)).save(any(TedTalk.class));
        assertEquals(1, job.getSuccessfulCount());
        assertEquals(1, job.getFailedCount());
    }

    private TedTalkRecord createValidTedTalkRecord() {
        return TedTalkRecord.builder()
                .title("Test Talk")
                .speaker("Test Speaker")
                .date(LocalDate.now())
                .build();
    }

    private TedTalkRecord createInvalidTedTalkRecord() {
        return TedTalkRecord.builder()
                .title("") // Invalid empty title
                .speaker("Test Speaker")
                .date(LocalDate.now())
                .build();
    }

}

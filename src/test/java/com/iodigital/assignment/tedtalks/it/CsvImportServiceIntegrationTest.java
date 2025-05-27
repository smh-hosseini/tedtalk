package com.iodigital.assignment.tedtalks.it;

import com.iodigital.assignment.tedtalks.TestcontainersConfiguration;
import com.iodigital.assignment.tedtalks.common.io.FileUtils;
import com.iodigital.assignment.tedtalks.common.io.FileSystemResourceProvider;
import com.iodigital.assignment.tedtalks.importcsv.event.FileUploadEvent;
import com.iodigital.assignment.tedtalks.importcsv.model.ImportJob;
import com.iodigital.assignment.tedtalks.importcsv.model.ImportJob.Status;
import com.iodigital.assignment.tedtalks.talk.model.TedTalk;
import com.iodigital.assignment.tedtalks.importcsv.repository.ImportJobRepository;
import com.iodigital.assignment.tedtalks.talk.repository.TedTalkRepository;
import com.iodigital.assignment.tedtalks.importcsv.service.CsvImportService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "tedtalks.csv.import.path=classpath:data",
    "tedtalks.csv.import.batch-size=10"
})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ContextConfiguration(initializers = {TestcontainersConfiguration.Initializer.class})
class CsvImportServiceIntegrationTest {

    @Autowired
    private CsvImportService csvImportService;

    @Autowired
    private ImportJobRepository importJobRepository;

    @Autowired
    private TedTalkRepository tedTalkRepository;

    @Autowired
    private FileSystemResourceProvider fileResourceProvider;

    @TempDir
    Path tempDir;

    private Path testCsvFile;
    private ImportJob importJob;

    @BeforeEach
    void setUp() throws IOException {
        // Clean up any existing data
        tedTalkRepository.deleteAll();
        importJobRepository.deleteAll();
    }

    @Test
    void shouldCompleteFullImportWorkflow() throws IOException {
        // Given
        createValidCsvFile();
        importJob = createImportJob(testCsvFile.toString());

        // When
        csvImportService.startImportJob(importJob);

        // Then
        ImportJob savedJob = importJobRepository.findById(importJob.getId()).orElseThrow();
        assertThat(savedJob.getStatus()).isEqualTo(Status.COMPLETED);
        assertThat(savedJob.getSuccessfulCount()).isEqualTo(3);
        assertThat(savedJob.getFailedCount()).isEqualTo(0);
        assertThat(savedJob.getSkippedCount()).isEqualTo(0);
        assertThat(savedJob.getLastProcessedLine()).isEqualTo(4);

        // Verify talks were saved
        List<TedTalk> savedTalks = tedTalkRepository.findAll();
        assertThat(savedTalks).hasSize(3);
        assertThat(savedTalks)
                .extracting(TedTalk::getTitle)
                .containsExactlyInAnyOrder(
                        "The Power of Vulnerability",
                        "Your Body Language May Shape Who You Are",
                        "How Great Leaders Inspire Action"
                );
    }

    @Test
    void shouldHandleDuplicateRecords() throws IOException {
        // Given
        createValidCsvFile();

        // Pre-populate with one existing talk
        TedTalk existingTalk = TedTalk.builder()
                .title("The Power of Vulnerability")
                .speaker("Brené Brown")
                .date(LocalDate.of(2010, 6, 1))
                .views(45000000L)
                .likes(125000L)
                .link("https://www.ted.com/talks/brene_brown_the_power_of_vulnerability")
                .build();
        tedTalkRepository.save(existingTalk);

        importJob = createImportJob(testCsvFile.toString());

        // When
        csvImportService.startImportJob(importJob);

        // Then
        ImportJob savedJob = importJobRepository.findById(importJob.getId()).orElseThrow();
        assertThat(savedJob.getStatus()).isEqualTo(Status.COMPLETED);
        assertThat(savedJob.getSuccessfulCount()).isEqualTo(2); // 2 new talks
        assertThat(savedJob.getSkippedCount()).isEqualTo(1); // 1 duplicate
        assertThat(savedJob.getFailedCount()).isEqualTo(0);

        // Verify total talks (3 from CSV, but 1 was duplicate)
        List<TedTalk> allTalks = tedTalkRepository.findAll();
        assertThat(allTalks).hasSize(3);
    }

    @Test
    void shouldHandleInvalidRecords() throws IOException {
        // Given
        createInvalidCsvFile();
        importJob = createImportJob(testCsvFile.toString());

        // When
        csvImportService.startImportJob(importJob);

        // Then
        ImportJob savedJob = importJobRepository.findById(importJob.getId()).orElseThrow();
        assertThat(savedJob.getStatus()).isEqualTo(Status.COMPLETED);
        assertThat(savedJob.getSuccessfulCount()).isEqualTo(1); // Only 1 valid record
        assertThat(savedJob.getFailedCount()).isEqualTo(2); // 2 invalid records
        assertThat(savedJob.getSkippedCount()).isEqualTo(0);

        // Verify only valid talk was saved
        List<TedTalk> savedTalks = tedTalkRepository.findAll();
        assertThat(savedTalks).hasSize(1);
        assertThat(savedTalks.get(0).getTitle()).isEqualTo("Valid Talk");
    }

    @Test
    void shouldResumeFromLastProcessedLine() throws IOException {
        // Given
        createLargeCsvFile(50); // 50 records
        importJob = createImportJob(testCsvFile.toString());
        importJob.setLastProcessedLine(30); // Simulate partial processing

        // When
        csvImportService.startImportJob(importJob);

        // Then
        ImportJob savedJob = importJobRepository.findById(importJob.getId()).orElseThrow();
        assertThat(savedJob.getStatus()).isEqualTo(Status.COMPLETED);
        assertThat(savedJob.getLastProcessedLine()).isEqualTo(51);
        assertThat(savedJob.getSuccessfulCount()).isEqualTo(20); // Only processed last 20 records

        // Verify only 20 talks were saved (skipped first 30)
        List<TedTalk> savedTalks = tedTalkRepository.findAll();
        assertThat(savedTalks).hasSize(20);
    }

    @Test
    void shouldHandleBatchProcessing() throws IOException {
        // Given
        createLargeCsvFile(25); // 25 records
        importJob = createImportJob(testCsvFile.toString());

        // When
        csvImportService.startImportJob(importJob);

        // Then
        ImportJob savedJob = importJobRepository.findById(importJob.getId()).orElseThrow();
        assertThat(savedJob.getStatus()).isEqualTo(Status.COMPLETED);
        assertThat(savedJob.getLastProcessedLine()).isEqualTo(26);
        assertThat(savedJob.getSuccessfulCount()).isEqualTo(25);

        // Verify job was saved multiple times during processing (batch updates)
        // This tests that the job state is persisted incrementally
        List<TedTalk> savedTalks = tedTalkRepository.findAll();
        assertThat(savedTalks).hasSize(25);
    }

    @Test
    void shouldFailOnCorruptedCsvFile() throws IOException {
        // Given
        createCorruptedCsvFile();
        importJob = createImportJob(testCsvFile.toString());

        // When
        csvImportService.startImportJob(importJob);

        // Then
        ImportJob savedJob = importJobRepository.findById(importJob.getId()).orElseThrow();
        assertThat(savedJob.getStatus()).isEqualTo(Status.COMPLETED);
        assertThat(savedJob.getSuccessfulCount()).isEqualTo(2);

        // Verify no talks were saved
        List<TedTalk> savedTalks = tedTalkRepository.findAll();
        assertThat(savedTalks).hasSize(2);
    }

    // Helper methods for creating test data

    private void createValidCsvFile() throws IOException {
        testCsvFile = tempDir.resolve("valid_talks.csv");
        List<String> csvContent = Arrays.asList(
                "title,author,date,views,likes,link",
                "The Power of Vulnerability,Brené Brown,June 2010,45000000,125000,https://www.ted.com/talks/brene_brown_the_power_of_vulnerability",
                "Your Body Language May Shape Who You Are,Amy Cuddy,May 2012,35000000,95000,https://www.ted.com/talks/amy_cuddy_your_body_language_may_shape_who_you_are",
                "How Great Leaders Inspire Action,Simon Sinek,May 2009,50000000,150000,https://www.ted.com/talks/simon_sinek_how_great_leaders_inspire_action"
        );
        Files.write(testCsvFile, csvContent, StandardCharsets.UTF_8);
    }

    private void createInvalidCsvFile() throws IOException {
        testCsvFile = tempDir.resolve("invalid_talks.csv");
        List<String> csvContent = Arrays.asList(
                "title,author,date,views,likes,link",
                ",Brené Brown,June 2012,45000000,125000,https://www.ted.com/talks/talk1", // Missing title
                "Valid Talk,Valid Speaker,June 2012,1000000,5000,https://www.ted.com/talks/talk2", // Valid record
                "Another Talk,,May 2012,35000000,95000,https://www.ted.com/talks/talk3" // Missing speaker
        );
        Files.write(testCsvFile, csvContent, StandardCharsets.UTF_8);
    }

    private void createLargeCsvFile(int recordCount) throws IOException {
        testCsvFile = tempDir.resolve("large_talks.csv");
        List<String> csvContent = new ArrayList<>();
        csvContent.add("title,author,date,views,likes,link");

        for (int i = 1; i <= recordCount; i++) {
            csvContent.add("Talk %d,Speaker %d,May 2020,%d,%d,https://www.ted.com/talks/talk-%d"
                    .formatted(i, i, (i % 28) + 1, i * 1000, i * 10, i));
        }

        Files.write(testCsvFile, csvContent, StandardCharsets.UTF_8);
    }

    private void createCorruptedCsvFile() throws IOException {
        testCsvFile = tempDir.resolve("corrupted_talks.csv");
        List<String> csvContent = Arrays.asList(
                "title,author,date,views,likes,link",
                "Talk 1,Speaker 1,May 2020,1000,10,https://example.com/talk1",
                "Corrupted line with wrong number of columns",
                "Talk 2,Speaker 2,June 2020,2000,20,16,https://example.com/talk2"
        );
        Files.write(testCsvFile, csvContent, StandardCharsets.UTF_8);
    }

    private ImportJob createImportJob(String filePath) throws IOException {
        var hash = FileUtils.calculateStreamHash(fileResourceProvider.getInputStream(filePath)); // Ensure file exists)
        ImportJob job = new ImportJob();
        job.setFilePath(filePath);
        job.setFileHash(hash);
        job.setFileName(Paths.get(filePath).getFileName().toString());
        job.setStatus(Status.PENDING);
        job.setLastProcessedLine(0);
        return importJobRepository.save(job);
    }
}
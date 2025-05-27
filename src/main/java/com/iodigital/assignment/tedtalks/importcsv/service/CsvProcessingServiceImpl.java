package com.iodigital.assignment.tedtalks.importcsv.service;

import com.iodigital.assignment.tedtalks.common.mapper.TedTalkMapper;
import com.iodigital.assignment.tedtalks.importcsv.model.ImportJob;
import com.iodigital.assignment.tedtalks.talk.model.TedTalk;
import com.iodigital.assignment.tedtalks.common.config.CsvImportProperties;
import com.iodigital.assignment.tedtalks.importcsv.reader.TedTalkCSVReader;
import com.iodigital.assignment.tedtalks.importcsv.model.TedTalkRecord;
import com.iodigital.assignment.tedtalks.importcsv.repository.ImportJobRepository;
import com.iodigital.assignment.tedtalks.talk.repository.TedTalkRepository;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvProcessingServiceImpl implements CsvProcessingService {

    private final ImportJobRepository importJobRepository;
    private final TedTalkRepository tedTalkRepository;
    private final Validator validator;
    private final CsvImportProperties csvImportProperties;

    @Override
    public void processImportJob(ImportJob job, TedTalkCSVReader csvReader) throws CsvValidationException, IOException {
        resumeFromLastProcessedLine(csvReader, job);
        job.setStatus(ImportJob.Status.IN_PROGRESS);
        importJobRepository.save(job);
        List<TedTalkRecord> batch;
        while (!(batch = csvReader.readBatch(csvImportProperties.getBatchSize())).isEmpty()) {
            processBatch(job, batch);
            job.setLastProcessedLine((int) csvReader.getRecordsRead());
            importJobRepository.save(job);
        }
    }

    @Override
    public void processBatch(ImportJob job, List<TedTalkRecord> batch) {
        for (TedTalkRecord talkRecord : batch) {
            processRecord(job, talkRecord);
        }
    }


    public void processRecord(ImportJob job, TedTalkRecord talkRecord) {
        if (talkRecord.isEmpty()) {
            log.warn("Received Empty TedTalkRecord, skipping processing.");
            job.processAndFailed();
            return;
        }
        TedTalk talk = TedTalkMapper.mapToTedTalk(talkRecord);

        if (!isValid(talk)) {
            job.processAndFailed();
            return;
        }

        if (isDuplicate(talk)) {
            job.processAndSkipped();
        } else {
            tedTalkRepository.save(talk);
            job.processAndSucceed();
        }
    }

    private void resumeFromLastProcessedLine(TedTalkCSVReader reader, ImportJob job) throws IOException {
        if (job.getLastProcessedLine() > 0) {
            reader.skip(job.getLastProcessedLine());
        }
    }

    private boolean isDuplicate(TedTalk talk) {
        return tedTalkRepository
                .findByTitleAndSpeakerAndDate(talk.getTitle(), talk.getSpeaker(), talk.getDate())
                .isPresent();
    }

    private boolean isValid(TedTalk tedTalk) {
        Set<ConstraintViolation<TedTalk>> violations = validator.validate(tedTalk);
        if (!violations.isEmpty()) {
            log.info("Validation failed for tedTalk {} cause {}", tedTalk, violations);
            return false;
        }
        return true;
    }
}

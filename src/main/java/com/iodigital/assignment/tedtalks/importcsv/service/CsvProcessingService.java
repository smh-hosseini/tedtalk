package com.iodigital.assignment.tedtalks.importcsv.service;

import com.iodigital.assignment.tedtalks.importcsv.model.ImportJob;
import com.iodigital.assignment.tedtalks.importcsv.reader.TedTalkCSVReader;
import com.iodigital.assignment.tedtalks.importcsv.model.TedTalkRecord;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.util.List;

public interface CsvProcessingService {

    void processImportJob(ImportJob job, TedTalkCSVReader csvReader) throws CsvValidationException, IOException;
    void processBatch(ImportJob job, List<TedTalkRecord> batch);


}

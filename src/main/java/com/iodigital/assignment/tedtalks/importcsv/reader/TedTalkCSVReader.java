package com.iodigital.assignment.tedtalks.importcsv.reader;

import com.iodigital.assignment.tedtalks.importcsv.exception.DataParsingException;
import com.iodigital.assignment.tedtalks.importcsv.model.TedTalkRecord;
import com.opencsv.*;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Slf4j
public class TedTalkCSVReader extends CSVReader {

    private static final List<String> REQUIRED_HEADERS = List.of("title", "author", "date", "views", "likes", "link");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    private final Map<String, Integer> headerMap;

    public TedTalkCSVReader(Reader reader) throws IOException, CsvValidationException {
        super(reader);
        this.parser = new RFC4180ParserBuilder().build();
        this.headerMap = readAndValidateHeaders();
    }

    private Map<String, Integer> readAndValidateHeaders() throws IOException, CsvValidationException {
        String[] headers = readNextRow();
        if (headers == null) {
            throw new CsvValidationException("CSV file is empty - no headers found");
        }

        Map<String, Integer> indexMap = buildHeaderIndexMap(headers);
        validateRequiredHeaders(indexMap);

        log.debug("CSV headers validated successfully: {}", Arrays.toString(headers));
        return indexMap;
    }

    private String[] readNextRow() throws IOException, CsvValidationException {
        return super.readNext();
    }


    private Map<String, Integer> buildHeaderIndexMap(String[] headers) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            String normalizedHeader = headers[i].trim().toLowerCase();
            map.put(normalizedHeader, i);
        }
        return map;
    }

    private void validateRequiredHeaders(Map<String, Integer> headerMap) throws CsvValidationException {
        List<String> missingHeaders = REQUIRED_HEADERS.stream()
                .filter(header -> !headerMap.containsKey(header))
                .sorted().toList();

        if (!missingHeaders.isEmpty()) {
            throw new CsvValidationException("Missing required headers: " + String.join(", ", missingHeaders));
        }
    }

    public TedTalkRecord readNextTedTalk() throws IOException, CsvValidationException {
        String[] row = super.readNext();
        log.info("Reading row: {}", Arrays.toString(row));
        if (row == null) {
            return null;
        }

        try {
            return buildTedTalkRecord(row);
        } catch (DataParsingException e) {
            log.error("Error processing row {}: {}", getRecordsRead(), e.getMessage());
            return new TedTalkRecord(); // Return an empty record to skip this row
        }
    }

    private TedTalkRecord buildTedTalkRecord(String[] row) throws DataParsingException {
        try {
            return TedTalkRecord.builder()
                    .title(getRequiredField(row, "title"))
                    .speaker(getRequiredField(row, "author"))
                    .date(parseDate(getRequiredField(row, "date")))
                    .views(parseLong(row, "views"))
                    .likes(parseLong(row, "likes"))
                    .link(getRequiredField(row, "link"))
                    .build();
        } catch (Exception e) {
            throw new DataParsingException("Failed to parse row data: " + e.getMessage(), e);
        }
    }

    public List<TedTalkRecord> readBatch(int batchSize) throws IOException, CsvValidationException {
        List<TedTalkRecord> batch = new ArrayList<>();
        TedTalkRecord currentTalk;

        while (batch.size() < batchSize && (currentTalk = readNextTedTalk()) != null) {
            batch.add(currentTalk);
        }

        return batch;
    }

    private String getRequiredField(String[] row, String fieldName) throws IllegalArgumentException {
        Integer index = headerMap.get(fieldName.toLowerCase());
        if (index == null || index >= row.length) {
            throw new IllegalArgumentException("Missing value for field: " + fieldName);
        }

        String value = row[index];
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }

        return value.trim();
    }

    private Long parseLong(String[] row, String fieldName) throws DataParsingException {
        try {
            String value = getRequiredField(row, fieldName);
            long parsedValue = Long.parseLong(value.replaceAll("[,\\s]", "")); // Remove commas and spaces
            if (parsedValue < 0) {
                throw new DataParsingException("Field '" + fieldName + "' must be non-negative, got: " + value);
            }
            return parsedValue;
        } catch (NumberFormatException e) {
            throw new DataParsingException("Invalid number format for field '" + fieldName, e);
        }
    }

    private LocalDate parseDate(String dateStr) throws DataParsingException {
        try {
            YearMonth yearMonth = YearMonth.parse(dateStr, DATE_FORMATTER);
            return yearMonth.atDay(1); // Default to the first day of the month
        } catch (DateTimeParseException e) {
            throw new DataParsingException("Invalid date format: " + dateStr + ". Expected format: 'MMMM yyyy'", e);
        }
    }
}
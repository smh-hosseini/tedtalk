package com.iodigital.assignment.tedtalks.importcsv.reader;

import com.opencsv.exceptions.CsvValidationException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Component
public class CsvReaderFactory {

    public TedTalkCSVReader createTedTalkReader(InputStream inputStream) throws IOException, CsvValidationException {
        InputStreamReader reader = new InputStreamReader(inputStream);
        return new TedTalkCSVReader(reader);
    }
}

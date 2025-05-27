package com.iodigital.assignment.tedtalks.importcsv.service;

import com.iodigital.assignment.tedtalks.common.config.CsvImportProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvResourceDiscoveryService {

    private final CsvImportProperties csvImportProperties;

    public List<Resource> discoverResources() {
        log.info("Discovering CSV files from classpath folder: {}", csvImportProperties.getPath());
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            return Arrays.stream(resolver.getResources(csvImportProperties.getPath() + "/*.csv")).toList();
        } catch (IOException ex) {
            log.error("Failed to read files from classpath - cause: {}", ex.getMessage(), ex);
            return List.of();
        }
    }
}

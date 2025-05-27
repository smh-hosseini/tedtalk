package com.iodigital.assignment.tedtalks.startup;

import com.iodigital.assignment.tedtalks.importcsv.service.CsvResourceDiscoveryService;
import com.iodigital.assignment.tedtalks.talk.service.ImportJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@ConditionalOnProperty(name = "tedtalks.csv.import.enabled", havingValue = "true")
@RequiredArgsConstructor
public class CsvImportOrchestrator {

    private final CsvResourceDiscoveryService resourceDiscoveryService;
    private final ImportJobService importJobService;

    @EventListener(ApplicationReadyEvent.class)
    public void importFilesOnStartup() {
        resourceDiscoveryService.discoverResources()
            .forEach(this::processResource);
    }

    private void processResource(Resource resource) {
        try {
            log.info("Importing CSV file: {}", resource.getFilename());
            importJobService.createImportJobForClassPathResource(resource);
            log.info("Successfully created A job: {}", resource.getFilename());
        } catch (Exception e) {
            log.error("Failed to import {}: {}", resource.getFilename(), e.getMessage(), e);
        }
    }
}

package com.iodigital.assignment.tedtalks.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@ConfigurationProperties(prefix = "tedtalks.csv.import")
public class CsvImportProperties {
    private boolean enabled = false;
    private String path = "classpath:data";
    private Integer batchSize = 100;
}

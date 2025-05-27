package com.iodigital.assignment.tedtalks.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({CsvImportProperties.class, InfluencerProperties.class})
public class AppConfig {
}

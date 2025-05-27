package com.iodigital.assignment.tedtalks.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tedtalks.influencer")
public class InfluencerProperties {

    private double viewsWeight;
    private double likesWeight;
}

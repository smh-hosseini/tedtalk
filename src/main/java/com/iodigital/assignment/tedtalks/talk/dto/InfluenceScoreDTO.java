package com.iodigital.assignment.tedtalks.talk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InfluenceScoreDTO {

    private String speaker;
    private Long totalTalks;
    private Long totalViews;
    private Long totalLikes;
    private Double influenceScore;

}
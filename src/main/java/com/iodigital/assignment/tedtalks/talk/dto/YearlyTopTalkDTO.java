package com.iodigital.assignment.tedtalks.talk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YearlyTopTalkDTO {

    private Long id;
    private Integer year;
    private String title;
    private String speaker;
    private Long views;
    private Long likes;
    private Double influenceScore;
}


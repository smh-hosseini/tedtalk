package com.iodigital.assignment.tedtalks.common.mapper;

import com.iodigital.assignment.tedtalks.talk.dto.InfluenceScoreDTO;
import com.iodigital.assignment.tedtalks.talk.dto.YearlyTopTalkDTO;
import com.iodigital.assignment.tedtalks.talk.repository.projection.InfluenceScore;
import com.iodigital.assignment.tedtalks.talk.repository.projection.YearlyTopTalk;

public class StatisticsMapper {

    private StatisticsMapper() {
        // Private constructor to prevent instantiation
    }

    /**
     * Converts an InfluenceScore object to an InfluenceScoreDTO.
     *
     * @param influenceScore the InfluenceScore object to convert
     * @return the converted InfluenceScoreDTO
     */
    public static InfluenceScoreDTO fromSpeakerInfluence(InfluenceScore influenceScore) {
        return InfluenceScoreDTO.builder()
                .speaker(influenceScore.getSpeaker())
                .totalTalks(influenceScore.getTalkCount())
                .totalViews(influenceScore.getTotalViews())
                .totalLikes(influenceScore.getTotalLikes())
                .influenceScore(influenceScore.getInfluenceScore())
                .build();
    }


    /**
     * Converts a YearlyTopTalk object to a YearlyTopTalkDTO.
     *
     * @param topTalk the YearlyTopTalk object to convert
     * @return the converted YearlyTopTalkDTO
     */
    public static YearlyTopTalkDTO fromTopTedTalksByYear(YearlyTopTalk topTalk) {
        return YearlyTopTalkDTO.builder()
                .year(topTalk.getYear())
                .id(topTalk.getTalkId())
                .title(topTalk.getTitle())
                .speaker(topTalk.getSpeaker())
                .views(topTalk.getTotalViews())
                .likes(topTalk.getTotalLikes())
                .influenceScore(topTalk.getInfluenceScore())
                .build();
    }
}

package com.iodigital.assignment.tedtalks.talk.service;


import com.iodigital.assignment.tedtalks.talk.dto.InfluenceScoreDTO;
import com.iodigital.assignment.tedtalks.talk.dto.YearlyTopTalkDTO;

import java.util.List;

public interface InfluenceAnalysisService {

    /**
     * Get the top N influential speakers
     *
     * @param limit number of speakers to return
     * @return list of influential speakers with their scores
     */
    List<InfluenceScoreDTO> getTopInfluentialSpeakers(int limit);

    /**
     * Get the most influential TedTalk for each year
     *
     * @return list of the most influential TedTalk per year
     */
    List<YearlyTopTalkDTO> getMostInfluentialTedTalkPerYear();
}

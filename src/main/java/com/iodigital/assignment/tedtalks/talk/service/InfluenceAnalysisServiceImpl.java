package com.iodigital.assignment.tedtalks.talk.service;

import com.iodigital.assignment.tedtalks.common.config.InfluencerProperties;
import com.iodigital.assignment.tedtalks.common.mapper.StatisticsMapper;
import com.iodigital.assignment.tedtalks.talk.dto.InfluenceScoreDTO;
import com.iodigital.assignment.tedtalks.talk.dto.YearlyTopTalkDTO;
import com.iodigital.assignment.tedtalks.talk.repository.TedTalkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class InfluenceAnalysisServiceImpl implements InfluenceAnalysisService {

    private final TedTalkRepository tedTalkRepository;

    private final InfluencerProperties influencerProperties;

    @Override
    public List<InfluenceScoreDTO> getTopInfluentialSpeakers(int limit) {
        final var speakers = tedTalkRepository.findTopInfluentialSpeakers(limit,
                influencerProperties.getViewsWeight(),
                influencerProperties.getLikesWeight());
        return speakers.stream().map(StatisticsMapper::fromSpeakerInfluence).toList();
    }

    @Override
    public List<YearlyTopTalkDTO> getMostInfluentialTedTalkPerYear() {

        final var topYearlyTedTalks = tedTalkRepository.findTopTedTalksPerYear(influencerProperties.getViewsWeight(),
                influencerProperties.getLikesWeight());
        return topYearlyTedTalks.stream().map(StatisticsMapper::fromTopTedTalksByYear).toList();
    }
}

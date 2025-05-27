package com.iodigital.assignment.tedtalks.service;

import com.iodigital.assignment.tedtalks.talk.dto.InfluenceScoreDTO;
import com.iodigital.assignment.tedtalks.talk.dto.YearlyTopTalkDTO;
import com.iodigital.assignment.tedtalks.talk.repository.TedTalkRepository;
import com.iodigital.assignment.tedtalks.talk.repository.projection.InfluenceScore;
import com.iodigital.assignment.tedtalks.talk.repository.projection.YearlyTopTalk;
import com.iodigital.assignment.tedtalks.talk.service.InfluenceAnalysisServiceImpl;
import com.iodigital.assignment.tedtalks.common.config.InfluencerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class InfluenceAnalysisServiceImplTest {

    @Mock
    private TedTalkRepository tedTalkRepository;

    @Mock
    private InfluencerProperties influencerProperties;

    @InjectMocks
    private InfluenceAnalysisServiceImpl influenceAnalysisService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up common properties
        when(influencerProperties.getViewsWeight()).thenReturn(0.7);
        when(influencerProperties.getLikesWeight()).thenReturn(0.3);
    }

    @Test
    void getTopInfluentialSpeakers_ShouldReturnCorrectMapping() {
        // Arrange
        int limit = 5;
        double viewsWeight = 0.7;
        double likesWeight = 0.3;

        // Create mock repository results
        InfluenceScore speaker1 = createSpeakerInfluence("John Doe", 150.5);
        InfluenceScore speaker2 = createSpeakerInfluence("Jane Smith", 120.0);
        var mockResults = Stream.of(speaker1, speaker2);

        when(tedTalkRepository.findTopInfluentialSpeakers(limit, viewsWeight, likesWeight))
            .thenReturn(mockResults.toList());

        // Act
        List<InfluenceScoreDTO> result = influenceAnalysisService.getTopInfluentialSpeakers(limit);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getSpeaker());
        assertEquals(150.5, result.get(0).getInfluenceScore());
        assertEquals("Jane Smith", result.get(1).getSpeaker());
        assertEquals(120.0, result.get(1).getInfluenceScore());

        verify(tedTalkRepository).findTopInfluentialSpeakers(limit, viewsWeight, likesWeight);
        verify(influencerProperties).getViewsWeight();
        verify(influencerProperties).getLikesWeight();
    }

    @Test
    void getMostInfluentialTedTalkPerYear_ShouldReturnCorrectMapping() {
        // Arrange
        double viewsWeight = 0.7;
        double likesWeight = 0.3;

        // Create mock repository results
        var talk2020 = createTopTedTalkByYear(2020, "AI Future", "John Doe", 200.0);
        var talk2021 = createTopTedTalkByYear(2021, "Climate Change", "Jane Smith", 180.0);
        List<YearlyTopTalk> mockResults = List.of(talk2020, talk2021);

        when(tedTalkRepository.findTopTedTalksPerYear(viewsWeight, likesWeight))
            .thenReturn(mockResults);

        // Act
        List<YearlyTopTalkDTO> result = influenceAnalysisService.getMostInfluentialTedTalkPerYear();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(2020, result.get(0).getYear());
        assertEquals("AI Future", result.get(0).getTitle());
        assertEquals("John Doe", result.get(0).getSpeaker());
        assertEquals(200.0, result.get(0).getInfluenceScore());

        assertEquals(2021, result.get(1).getYear());
        assertEquals("Climate Change", result.get(1).getTitle());
        assertEquals("Jane Smith", result.get(1).getSpeaker());
        assertEquals(180.0, result.get(1).getInfluenceScore());

        verify(tedTalkRepository).findTopTedTalksPerYear(viewsWeight, likesWeight);
        verify(influencerProperties).getViewsWeight();
        verify(influencerProperties).getLikesWeight();
    }

    // Helper methods to create test data
    private InfluenceScore createSpeakerInfluence(String speaker, double score) {
        return new InfluenceScore() {
            @Override
            public String getSpeaker() {
                return speaker;
            }

            @Override
            public Long getTalkCount() {
                return 0L;
            }

            @Override
            public Long getTotalViews() {
                return 0L;
            }

            @Override
            public Long getTotalLikes() {
                return 0L;
            }

            @Override
            public Double getInfluenceScore() {
                return score;
            }

        };
    }

    private YearlyTopTalk createTopTedTalkByYear(int year, String title, String speaker, double score) {
        return new YearlyTopTalk() {
            @Override
            public Integer getYear() {
                return year;
            }

            @Override
            public Long getTalkId() {
                return 1L;
            }

            @Override
            public String getTitle() {
                return title;
            }

            @Override
            public String getSpeaker() {
                return speaker;
            }

            @Override
            public Long getTalkCount() {
                return 0L;
            }

            @Override
            public Long getTotalViews() {
                return 0L;
            }

            @Override
            public Long getTotalLikes() {
                return 0L;
            }

            @Override
            public Double getInfluenceScore() {
                return score;
            }
        };
    }
}
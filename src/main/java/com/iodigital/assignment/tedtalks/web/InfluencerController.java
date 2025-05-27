package com.iodigital.assignment.tedtalks.web;

import com.iodigital.assignment.tedtalks.talk.dto.InfluenceScoreDTO;
import com.iodigital.assignment.tedtalks.talk.dto.YearlyTopTalkDTO;
import com.iodigital.assignment.tedtalks.talk.service.InfluenceAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/influence")
@RequiredArgsConstructor
public class InfluencerController {

    private final InfluenceAnalysisService influenceAnalysisService;

    @GetMapping("/speakers")
    public ResponseEntity<List<InfluenceScoreDTO>> getTopInfluentialSpeakers(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(influenceAnalysisService.getTopInfluentialSpeakers(limit));
    }

    @GetMapping("/yearly")
    public ResponseEntity<List<YearlyTopTalkDTO>> getMostInfluentialTedTalkPerYear() {
        return ResponseEntity.ok(influenceAnalysisService.getMostInfluentialTedTalkPerYear());
    }

}

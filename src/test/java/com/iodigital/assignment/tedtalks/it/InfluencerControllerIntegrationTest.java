package com.iodigital.assignment.tedtalks.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iodigital.assignment.tedtalks.TedtalksApplication;
import com.iodigital.assignment.tedtalks.TestcontainersConfiguration;
import com.iodigital.assignment.tedtalks.talk.model.TedTalk;
import com.iodigital.assignment.tedtalks.talk.repository.TedTalkRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TedtalksApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "tedtalks.csv.import.enabled=false",
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ContextConfiguration(initializers = {TestcontainersConfiguration.Initializer.class})
class InfluencerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TedTalkRepository tedTalkRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private List<TedTalk> sampleTedTalks;

    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules();

        // Create and save sample TedTalks with different speakers and years
        sampleTedTalks = new ArrayList<>();

        // Speaker 1 with multiple talks across years
        TedTalk talk1 = new TedTalk();
        talk1.setTitle("How great leaders inspire action");
        talk1.setSpeaker("Simon Sinek");
        talk1.setDate(LocalDate.of(2010, 5, 4));
        talk1.setViews(50000000L);
        talk1.setLikes(1500000L);
        talk1.setLink("https://www.ted.com/talks/simon_sinek_how_great_leaders_inspire_action");

        TedTalk talk2 = new TedTalk();
        talk2.setTitle("Why good leaders make you feel safe");
        talk2.setSpeaker("Simon Sinek");
        talk2.setDate(LocalDate.of(2014, 3, 6));
        talk2.setViews(20000000L);
        talk2.setLikes(800000L);
        talk2.setLink("https://www.ted.com/talks/simon_sinek_why_good_leaders_make_you_feel_safe");

        // Speaker 2
        TedTalk talk3 = new TedTalk();
        talk3.setTitle("The power of vulnerability");
        talk3.setSpeaker("Brené Brown");
        talk3.setDate(LocalDate.of(2011, 6, 15));
        talk3.setViews(60000000L);
        talk3.setLikes(2000000L);
        talk3.setLink("https://www.ted.com/talks/brene_brown_the_power_of_vulnerability");

        // Speaker 3 - different year
        TedTalk talk4 = new TedTalk();
        talk4.setTitle("Your body language may shape who you are");
        talk4.setSpeaker("Amy Cuddy");
        talk4.setDate(LocalDate.of(2012, 10, 1));
        talk4.setViews(70000000L);
        talk4.setLikes(2500000L);
        talk4.setLink("https://www.ted.com/talks/amy_cuddy_your_body_language_may_shape_who_you_are");

        sampleTedTalks.add(tedTalkRepository.save(talk1));
        sampleTedTalks.add(tedTalkRepository.save(talk2));
        sampleTedTalks.add(tedTalkRepository.save(talk3));
        sampleTedTalks.add(tedTalkRepository.save(talk4));
    }

    @AfterEach
    void tearDown() {
        tedTalkRepository.deleteAll();
    }

    @Test
    void getTopInfluentialSpeakers_ShouldReturnSpeakersWithScores() throws Exception {
        mockMvc.perform(get("/api/influence/speakers")
                .param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].speaker", containsInAnyOrder("Simon Sinek", "Brené Brown", "Amy Cuddy")))
                .andExpect(jsonPath("$[*].influenceScore", everyItem(greaterThan(0.0))));
    }

    @Test
    void getTopInfluentialSpeakers_WithDefaultLimit_ShouldReturnLimitedSpeakers() throws Exception {
        mockMvc.perform(get("/api/influence/speakers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(lessThanOrEqualTo(10))));
    }

    @Test
    void getMostInfluentialTedTalkPerYear_ShouldReturnTopTalksByYear() throws Exception {
        mockMvc.perform(get("/api/influence/yearly"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[*].year", containsInAnyOrder(2010, 2011, 2012, 2014)))
                .andExpect(jsonPath("$[*].title", containsInAnyOrder(
                        "How great leaders inspire action",
                        "The power of vulnerability",
                        "Your body language may shape who you are",
                        "Why good leaders make you feel safe"
                )))
                .andExpect(jsonPath("$[*].speaker", containsInAnyOrder(
                        "Simon Sinek",
                        "Simon Sinek",
                        "Brené Brown",
                        "Amy Cuddy"
                )))
                .andExpect(jsonPath("$[*].influenceScore", everyItem(greaterThan(0.0))));
    }
}
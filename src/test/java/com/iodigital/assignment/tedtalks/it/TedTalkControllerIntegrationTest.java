package com.iodigital.assignment.tedtalks.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iodigital.assignment.tedtalks.TedtalksApplication;
import com.iodigital.assignment.tedtalks.TestcontainersConfiguration;
import com.iodigital.assignment.tedtalks.talk.dto.TedTalkDTO;
import com.iodigital.assignment.tedtalks.talk.model.TedTalk;
import com.iodigital.assignment.tedtalks.talk.repository.TedTalkRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TedtalksApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "tedtalks.csv.import.enabled=false",
        "tedtalks.csv.import.batch-size=10"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ContextConfiguration(initializers = {TestcontainersConfiguration.Initializer.class})
class TedTalkControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TedTalkRepository tedTalkRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private List<TedTalk> sampleTedTalks;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);
        // Include the resolver in the MockMvc setup


        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        // Create and save sample TED talks
        sampleTedTalks = new ArrayList<>();

        TedTalk talk1 = new TedTalk();
        talk1.setTitle("Integration Test Talk 1");
        talk1.setSpeaker("Integration Tester 1");
        talk1.setDate(LocalDate.of(2022, 1, 1));
        talk1.setViews(50000L);
        talk1.setLikes(2500L);
        talk1.setLink("https://www.test.com/talks/1");

        TedTalk talk2 = new TedTalk();
        talk2.setTitle("Integration Test Talk 2");
        talk2.setSpeaker("Integration Tester 2");
        talk2.setDate(LocalDate.of(2022, 2, 1));
        talk2.setViews(75000L);
        talk2.setLikes(3500L);
        talk2.setLink("https://www.test.com/talks/2");

        sampleTedTalks.add(tedTalkRepository.save(talk1));
        sampleTedTalks.add(tedTalkRepository.save(talk2));
    }

    @AfterEach
    void tearDown() {
        tedTalkRepository.deleteAll();
    }

    @Test
    void getAllTedTalks_ShouldReturnAllTedTalks() throws Exception {
        mockMvc.perform(get("/api/v1/tedtalks")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].title", is("Integration Test Talk 2")))
                .andExpect(jsonPath("$.content[1].title", is("Integration Test Talk 1")));
    }

    @Test
    void getTedTalkById_WhenExists_ShouldReturnTedTalk() throws Exception {
        TedTalk tedTalk = sampleTedTalks.get(0);

        mockMvc.perform(get("/api/v1/tedtalks/{id}", tedTalk.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(tedTalk.getId().intValue())))
                .andExpect(jsonPath("$.title", is(tedTalk.getTitle())))
                .andExpect(jsonPath("$.speaker", is(tedTalk.getSpeaker())));
    }

    @Test
    void getTedTalkById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/tedtalks/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createTedTalk_WithValidData_ShouldCreateAndReturnTedTalk() throws Exception {
        TedTalkDTO newTalk = new TedTalkDTO();
        newTalk.setTitle("New Integration Talk");
        newTalk.setSpeaker("New Integration Speaker");
        newTalk.setDate(LocalDate.of(2023, 3, 15));
        newTalk.setViews(10000L);
        newTalk.setLikes(500L);
        newTalk.setLink("https://www.test.com/talks/new");

        mockMvc.perform(post("/api/v1/tedtalks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newTalk)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is(newTalk.getTitle())))
                .andExpect(jsonPath("$.speaker", is(newTalk.getSpeaker())));

        assertEquals(3, tedTalkRepository.count());
    }

    @Test
    void updateTedTalk_WhenExists_ShouldUpdateAndReturnTedTalk() throws Exception {
        TedTalk existingTalk = sampleTedTalks.get(0);

        TedTalkDTO updateDTO = new TedTalkDTO();
        updateDTO.setId(existingTalk.getId());
        updateDTO.setTitle("Updated Integration Talk");
        updateDTO.setSpeaker(existingTalk.getSpeaker());
        updateDTO.setDate(existingTalk.getDate());
        updateDTO.setViews(existingTalk.getViews());
        updateDTO.setLikes(existingTalk.getLikes());
        updateDTO.setLink(existingTalk.getLink());

        mockMvc.perform(put("/api/v1/tedtalks/{id}", existingTalk.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(existingTalk.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Updated Integration Talk")));

        TedTalk updated = tedTalkRepository.findById(existingTalk.getId()).orElseThrow();
        assertEquals("Updated Integration Talk", updated.getTitle());
    }

    @Test
    void updateTedTalk_WhenNotExists_ShouldReturnNotFound() throws Exception {
        TedTalkDTO updateDTO = new TedTalkDTO();
        updateDTO.setId(999L);
        updateDTO.setTitle("Nonexistent Talk");
        updateDTO.setSpeaker("Nonexistent Speaker");
        updateDTO.setViews(0L);
        updateDTO.setLikes(0L);
        updateDTO.setLink("https://www.test.com/talks/nonexistent");
        updateDTO.setDate(LocalDate.now());

        mockMvc.perform(put("/api/v1/tedtalks/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTedTalk_WhenExists_ShouldDeleteAndReturnNoContent() throws Exception {
        TedTalk tedTalk = sampleTedTalks.get(0);
        Long id = tedTalk.getId();

        mockMvc.perform(delete("/api/v1/tedtalks/{id}", id))
                .andExpect(status().isNoContent());

        assertFalse(tedTalkRepository.existsById(id));
    }

    @Test
    void deleteTedTalk_WhenNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/tedtalks/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void importTedTalks_WithValidFile_ShouldImportDataAndReturnOk() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-import.csv",
                "text/csv",
                """
                title,author,date,views,likes,link
                Imported Talk 1,Import Speaker 1,May 2023,20000,1000,https://test.com/import1
                Imported Talk 2,Import Speaker 2,May 2023,30000,1500,https://test.com/import2
                """
                        .getBytes()
        );

        mockMvc.perform(multipart("/api/v1/tedtalks/import")
                        .file(file))
                .andExpect(status().isOk());

        // Verify the data was imported
        assertTrue(tedTalkRepository.findByTitle("Imported Talk 1").isPresent());
        assertTrue(tedTalkRepository.findByTitle("Imported Talk 2").isPresent());
    }

    @Test
    void importTedTalks_WithEmptyFile_ShouldReturnBadRequest() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.csv",
                "text/csv",
                "".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/tedtalks/import")
                .file(emptyFile))
                .andExpect(status().isBadRequest());
    }

}
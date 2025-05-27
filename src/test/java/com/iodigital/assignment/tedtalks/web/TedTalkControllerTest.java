package com.iodigital.assignment.tedtalks.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iodigital.assignment.tedtalks.talk.dto.ImportFileDTO;
import com.iodigital.assignment.tedtalks.talk.dto.TedTalkDTO;
import com.iodigital.assignment.tedtalks.talk.service.TedTalkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TedTalkControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TedTalkService tedTalkService;

    @InjectMocks
    private TedTalkController tedTalkController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        PageableHandlerMethodArgumentResolver pageableResolver = new PageableHandlerMethodArgumentResolver();

        mockMvc = MockMvcBuilders.standaloneSetup(tedTalkController)
                .setCustomArgumentResolvers(pageableResolver)
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Support for Java 8 date/time types
    }

    @Test
    void getAllTedTalks_ShouldReturnPageOfTedTalks() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<TedTalkDTO> talks = List.of(
                createTedTalkDTO(1L, "Talk 1", "Speaker 1"),
                createTedTalkDTO(2L, "Talk 2", "Speaker 2")
        );
        Page<TedTalkDTO> page = new PageImpl<>(talks, pageable, talks.size());

        when(tedTalkService.getAllTedTalks(any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/tedtalks")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Talk 1"))
                .andExpect(jsonPath("$.content[1].id").value(2));

        verify(tedTalkService).getAllTedTalks(any(Pageable.class));
    }

    @Test
    void getTedTalkById_WhenExists_ShouldReturnTedTalk() throws Exception {
        // Arrange
        Long id = 1L;
        TedTalkDTO tedTalk = createTedTalkDTO(id, "How to Test", "Test Speaker");

        when(tedTalkService.getTedTalkById(id)).thenReturn(Optional.of(tedTalk));

        // Act & Assert
        mockMvc.perform(get("/api/v1/tedtalks/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("How to Test"));

        verify(tedTalkService).getTedTalkById(id);
    }

    @Test
    void getTedTalkById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        Long id = 999L;
        when(tedTalkService.getTedTalkById(id)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/tedtalks/{id}", id))
                .andExpect(status().isNotFound());

        verify(tedTalkService).getTedTalkById(id);
    }

    @Test
    void createTedTalk_WithValidData_ShouldReturnCreated() throws Exception {
        // Arrange
        TedTalkDTO inputTalk = createTedTalkDTO(null, "New Talk", "New Speaker");
        TedTalkDTO savedTalk = createTedTalkDTO(1L, "New Talk", "New Speaker");

        when(tedTalkService.createTedTalk(any(TedTalkDTO.class))).thenReturn(savedTalk);

        // Act & Assert
        mockMvc.perform(post("/api/v1/tedtalks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputTalk)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New Talk"));

        verify(tedTalkService).createTedTalk(any(TedTalkDTO.class));
    }

    @Test
    void updateTedTalk_WhenExists_ShouldReturnUpdatedTalk() throws Exception {
        // Arrange
        Long id = 1L;
        TedTalkDTO updateTalk = createTedTalkDTO(id, "Updated Talk", "Updated Speaker");

        when(tedTalkService.updateTedTalk(eq(id), any(TedTalkDTO.class))).thenReturn(Optional.of(updateTalk));

        // Act & Assert
        mockMvc.perform(put("/api/v1/tedtalks/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTalk)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Updated Talk"));

        verify(tedTalkService).updateTedTalk(eq(id), any(TedTalkDTO.class));
    }

    @Test
    void updateTedTalk_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        Long id = 999L;
        TedTalkDTO updateTalk = createTedTalkDTO(id, "Updated Talk", "Updated Speaker");

        when(tedTalkService.updateTedTalk(eq(id), any(TedTalkDTO.class))).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/api/v1/tedtalks/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTalk)))
                .andExpect(status().isNotFound());

        verify(tedTalkService).updateTedTalk(eq(id), any(TedTalkDTO.class));
    }

    @Test
    void deleteTedTalk_WhenExists_ShouldReturnNoContent() throws Exception {
        // Arrange
        Long id = 1L;
        when(tedTalkService.deleteTedTalk(id)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/tedtalks/{id}", id))
                .andExpect(status().isNoContent());

        verify(tedTalkService).deleteTedTalk(id);
    }

    @Test
    void deleteTedTalk_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        Long id = 999L;
        when(tedTalkService.deleteTedTalk(id)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/tedtalks/{id}", id))
                .andExpect(status().isNotFound());

        verify(tedTalkService).deleteTedTalk(id);
    }

    @Test
    void importTedTalks_WithValidFile_ShouldReturnOk() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                "title,speaker,date\nTest Talk,Test Speaker,May 2020".getBytes()
        );

        when(tedTalkService.importTedTalksFromCsv(any(MockMultipartFile.class))).thenReturn(any(ImportFileDTO.class));

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/tedtalks/import")
                .file(file))
                .andExpect(status().isOk());

        verify(tedTalkService).importTedTalksFromCsv(any(MockMultipartFile.class));
    }

    @Test
    void importTedTalks_WithEmptyFile_ShouldReturnBadRequest() throws Exception {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.csv",
                "text/csv",
                "".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/tedtalks/import")
                .file(emptyFile))
                .andExpect(status().isBadRequest());

        verify(tedTalkService, never()).importTedTalksFromCsv(any());
    }

    // Helper method to create a TedTalkDTO for testing
    private TedTalkDTO createTedTalkDTO(Long id, String title, String speaker) {
        TedTalkDTO dto = new TedTalkDTO();
        dto.setId(id);
        dto.setTitle(title);
        dto.setSpeaker(speaker);
        dto.setDate(LocalDate.of(2023, 1, 1));
        dto.setViews(10000L);
        dto.setLikes(500L);
        dto.setLink("https://example.com/talk/");
        return dto;
    }
}
package com.iodigital.assignment.tedtalks.service;

import com.iodigital.assignment.tedtalks.talk.dto.TedTalkDTO;
import com.iodigital.assignment.tedtalks.talk.model.TedTalk;
import com.iodigital.assignment.tedtalks.talk.repository.TedTalkRepository;
import com.iodigital.assignment.tedtalks.talk.service.TedTalkServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TedTalkServiceTest {

    @Mock
    private TedTalkRepository tedTalkRepository;


    @InjectMocks
    private TedTalkServiceImpl tedTalkService;

    private TedTalk sampleTedTalk;
    private TedTalkDTO sampleTedTalkDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create sample data
        sampleTedTalk = TedTalk.builder()
                .id(1L)
                .title("Sample Talk")
                .speaker("John Doe")
                .date(LocalDate.of(2020, 1, 1))
                .views(1000L)
                .likes(500L)
                .build();

        sampleTedTalkDTO = TedTalkDTO.builder()
                .id(1L)
                .title("Sample Talk")
                .speaker("John Doe")
                .date(LocalDate.of(2020, 1, 1))
                .views(1000L)
                .likes(500L)
                .build();
    }

    @Test
    void deleteTedTalk_WhenNotExists_ShouldReturnFalse() {
        // Arrange
        when(tedTalkRepository.existsById(999L)).thenReturn(false);

        // Act
        boolean result = tedTalkService.deleteTedTalk(999L);

        // Assert
        assertFalse(result);
        verify(tedTalkRepository, times(1)).existsById(999L);
        verify(tedTalkRepository, never()).deleteById(anyLong());
    }




    @Test
    void getTedTalkById_WhenExists_ShouldReturnTedTalk() {
        // Arrange
        when(tedTalkRepository.findById(1L)).thenReturn(Optional.of(sampleTedTalk));

        // Act
        Optional<TedTalkDTO> result = tedTalkService.getTedTalkById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(sampleTedTalkDTO.getTitle(), result.get().getTitle());
        assertEquals(sampleTedTalkDTO.getSpeaker(), result.get().getSpeaker());
        verify(tedTalkRepository, times(1)).findById(1L);
    }

    @Test
    void getTedTalkById_WhenNotExists_ShouldReturnEmpty() {
        // Arrange
        when(tedTalkRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<TedTalkDTO> result = tedTalkService.getTedTalkById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(tedTalkRepository, times(1)).findById(999L);
    }

    @Test
    void createTedTalk_ShouldSaveAndReturnTedTalk() {
        // Arrange
        when(tedTalkRepository.save(any(TedTalk.class))).thenReturn(sampleTedTalk);

        // Act
        TedTalkDTO result = tedTalkService.createTedTalk(sampleTedTalkDTO);

        // Assert
        assertNotNull(result);
        assertEquals(sampleTedTalkDTO.getTitle(), result.getTitle());
        assertEquals(sampleTedTalkDTO.getSpeaker(), result.getSpeaker());
        verify(tedTalkRepository, times(1)).save(any(TedTalk.class));
    }

    @Test
    void updateTedTalk_WhenExists_ShouldUpdateAndReturnTedTalk() {
        // Arrange
        when(tedTalkRepository.existsById(1L)).thenReturn(true);
        when(tedTalkRepository.save(any(TedTalk.class))).thenReturn(sampleTedTalk);

        // Act
        Optional<TedTalkDTO> result = tedTalkService.updateTedTalk(1L, sampleTedTalkDTO);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(sampleTedTalkDTO.getTitle(), result.get().getTitle());
        assertEquals(sampleTedTalkDTO.getSpeaker(), result.get().getSpeaker());
        verify(tedTalkRepository, times(1)).existsById(1L);
        verify(tedTalkRepository, times(1)).save(any(TedTalk.class));
    }

    @Test
    void updateTedTalk_WhenNotExists_ShouldReturnEmpty() {
        // Arrange
        when(tedTalkRepository.existsById(999L)).thenReturn(false);

        // Act
        Optional<TedTalkDTO> result = tedTalkService.updateTedTalk(999L, sampleTedTalkDTO);

        // Assert
        assertFalse(result.isPresent());
        verify(tedTalkRepository, times(1)).existsById(999L);
        verify(tedTalkRepository, never()).save(any(TedTalk.class));
    }

}
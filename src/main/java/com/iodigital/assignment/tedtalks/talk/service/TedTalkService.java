package com.iodigital.assignment.tedtalks.talk.service;

import com.iodigital.assignment.tedtalks.talk.dto.ImportFileDTO;
import com.iodigital.assignment.tedtalks.talk.dto.TedTalkDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface TedTalkService {

    /**
     * Get all TedTalks with pagination
     *
     * @param pageable pagination information
     * @return paginated list of TedTalks
     */
    Page<TedTalkDTO> getAllTedTalks(Pageable pageable);

    /**
     * Get all TedTalks by a specific speaker
     *
     * @param speaker the speaker's name
     * @return list of TedTalks by the specified speaker
     */
    List<TedTalkDTO> getAllTedTalksBySpeaker(String speaker);

    /**
     * Get a TedTalk by ID
     *
     * @param id the TedTalk ID
     * @return the TedTalk if found
     */
    Optional<TedTalkDTO> getTedTalkById(Long id);

    /**
     * Create a new TedTalk
     *
     * @param tedTalkDTO the TedTalk to create
     * @return the created TedTalk
     */
    TedTalkDTO createTedTalk(TedTalkDTO tedTalkDTO);

    /**
     * Update an existing TedTalk
     *
     * @param id the TedTalk ID
     * @param tedTalkDTO the updated TedTalk data
     * @return the updated TedTalk if found
     */
    Optional<TedTalkDTO> updateTedTalk(Long id, TedTalkDTO tedTalkDTO);

    /**
     * Delete a TedTalk by ID
     *
     * @param id the TedTalk ID
     * @return true if deleted, false if not found
     */
    boolean deleteTedTalk(Long id);

    /**
     * Import TedTalks from a CSV file
     *
     * @param file the CSV file to import
     * @return ImportFileDTO containing the result of the import operation
     */
    ImportFileDTO importTedTalksFromCsv(MultipartFile file);

}

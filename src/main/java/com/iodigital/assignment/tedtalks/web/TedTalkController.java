package com.iodigital.assignment.tedtalks.web;

import com.iodigital.assignment.tedtalks.talk.dto.ImportFileDTO;
import com.iodigital.assignment.tedtalks.talk.dto.TedTalkDTO;
import com.iodigital.assignment.tedtalks.talk.service.TedTalkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/api/v1/tedtalks")
@RequiredArgsConstructor
@Slf4j
public class TedTalkController {

    private static final String CSV_MIME_TYPE = "text/csv";

    private final TedTalkService tedTalkService;

    // TedTalks Management Endpoints
    @GetMapping
    public ResponseEntity<Page<TedTalkDTO>> getAllTedTalks(
            @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(tedTalkService.getAllTedTalks(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TedTalkDTO> getTedTalkById(@PathVariable Long id) {
        return tedTalkService.getTedTalkById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TedTalkDTO> createTedTalk(@Valid @RequestBody TedTalkDTO tedTalkDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tedTalkService.createTedTalk(tedTalkDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TedTalkDTO> updateTedTalk(
            @PathVariable Long id,
            @Valid @RequestBody TedTalkDTO tedTalkDTO) {
        return tedTalkService.updateTedTalk(id, tedTalkDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTedTalk(@PathVariable Long id) {
        if (tedTalkService.deleteTedTalk(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Data Import Endpoint
    @PostMapping( value= "/import", consumes = "multipart/form-data")
    public ResponseEntity<ImportFileDTO> importTedTalks(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(ImportFileDTO.builder().response("File is empty").build());
        } else {
            file.getName();
        }

        // Check MIME type
        if (!CSV_MIME_TYPE.equals(file.getContentType())) {
            return ResponseEntity
                    .badRequest()
                    .body(ImportFileDTO.builder().response("Only CSV files are allowed").build());
        }

        final var response = tedTalkService.importTedTalksFromCsv(file);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/by-speaker")
    public ResponseEntity<List<TedTalkDTO>> getAllTedTalksBySpeaker(@RequestParam(required = false) String speaker) {
        return ResponseEntity.ok(tedTalkService.getAllTedTalksBySpeaker(speaker));
    }


}
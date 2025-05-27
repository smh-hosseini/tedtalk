package com.iodigital.assignment.tedtalks.talk.service;

import com.iodigital.assignment.tedtalks.talk.dto.ImportFileDTO;
import com.iodigital.assignment.tedtalks.talk.dto.TedTalkDTO;
import com.iodigital.assignment.tedtalks.talk.model.TedTalk;
import com.iodigital.assignment.tedtalks.common.mapper.TedTalkMapper;
import com.iodigital.assignment.tedtalks.talk.repository.TedTalkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static com.iodigital.assignment.tedtalks.common.mapper.TedTalkMapper.mapToDTO;
import static com.iodigital.assignment.tedtalks.common.mapper.TedTalkMapper.mapToEntity;

@Slf4j
@Service
@RequiredArgsConstructor
public class TedTalkServiceImpl implements TedTalkService {

    private final TedTalkRepository tedTalkRepository;
    private final ImportJobService importJobService;


    @Override
    @Transactional(readOnly = true)
    public Page<TedTalkDTO> getAllTedTalks(Pageable pageable) {
        return tedTalkRepository.findAll(pageable).map(TedTalkMapper::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TedTalkDTO> getAllTedTalksBySpeaker(String speaker) {
        return tedTalkRepository.findAllBySpeaker(speaker)
                .map(TedTalkMapper::mapToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TedTalkDTO> getTedTalkById(Long id) {
        return tedTalkRepository.findById(id)
                .map(TedTalkMapper::mapToDTO);
    }

    @Override
    @Transactional
    public TedTalkDTO createTedTalk(TedTalkDTO tedTalkDTO) {
        TedTalk tedTalk = mapToEntity(tedTalkDTO);
        tedTalk = tedTalkRepository.save(tedTalk);
        return mapToDTO(tedTalk);
    }

    @Override
    @Transactional
    public Optional<TedTalkDTO> updateTedTalk(Long id, TedTalkDTO tedTalkDTO) {
        if (!tedTalkRepository.existsById(id)) {
            return Optional.empty();
        }

        TedTalk tedTalk = mapToEntity(tedTalkDTO);
        tedTalk.setId(id);
        tedTalk = tedTalkRepository.save(tedTalk);
        return Optional.of(mapToDTO(tedTalk));
    }

    @Override
    @Transactional
    public boolean deleteTedTalk(Long id) {
        if (!tedTalkRepository.existsById(id)) {
            return false;
        }

        tedTalkRepository.deleteById(id);
        return true;
    }

    @Override
    @Transactional
    public ImportFileDTO importTedTalksFromCsv(MultipartFile file) {
        final var job = importJobService.createImportJob(file);
        return ImportFileDTO.builder().jobId(job.getId()).build();
    }
}

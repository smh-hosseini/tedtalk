package com.iodigital.assignment.tedtalks.common.mapper;

import com.iodigital.assignment.tedtalks.talk.dto.TedTalkDTO;
import com.iodigital.assignment.tedtalks.talk.model.TedTalk;
import com.iodigital.assignment.tedtalks.importcsv.model.TedTalkRecord;

public class TedTalkMapper {

    private TedTalkMapper() {
    }

    /**
     * Map a TedTalkRecord to a TedTalk entity
     *
     * @param tedTalkRecord the tedTalkRecord to map
     * @return the mapped entity
     */
    public static TedTalk mapToTedTalk(TedTalkRecord tedTalkRecord) {
        return TedTalk.builder()
                .title(tedTalkRecord.getTitle())
                .speaker(tedTalkRecord.getSpeaker())
                .date(tedTalkRecord.getDate())
                .views(tedTalkRecord.getViews())
                .likes(tedTalkRecord.getLikes())
                .link(tedTalkRecord.getLink())
                .build();
    }

    /**
     * Map a TedTalk entity to a TedTalkDTO
     *
     * @param tedTalk the entity to map
     * @return the mapped DTO
     */
    public static TedTalkDTO mapToDTO(TedTalk tedTalk) {
        return TedTalkDTO.builder()
                .id(tedTalk.getId())
                .title(tedTalk.getTitle())
                .speaker(tedTalk.getSpeaker())
                .date(tedTalk.getDate())
                .views(tedTalk.getViews())
                .likes(tedTalk.getLikes())
                .link(tedTalk.getLink())
                .build();
    }

    /**
     * Map a TedTalkDTO to a TedTalk entity
     *
     * @param tedTalkDTO the DTO to map
     * @return the mapped entity
     */
    public static TedTalk mapToEntity(TedTalkDTO tedTalkDTO) {
        return TedTalk.builder()
                .id(tedTalkDTO.getId())
                .title(tedTalkDTO.getTitle())
                .speaker(tedTalkDTO.getSpeaker())
                .date(tedTalkDTO.getDate())
                .views(tedTalkDTO.getViews())
                .likes(tedTalkDTO.getLikes())
                .link(tedTalkDTO.getLink())
                .build();
    }
}

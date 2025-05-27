package com.iodigital.assignment.tedtalks.talk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TedTalkDTO {

    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Speaker is required")
    private String speaker;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Views count is required")
    @PositiveOrZero(message = "Views count must be a positive number or zero")
    private Long views;

    @NotNull(message = "Likes count is required")
    @PositiveOrZero(message = "Likes count must be a positive number or zero")
    private Long likes;

    @NotBlank(message = "Link is required")
    private String link;
}
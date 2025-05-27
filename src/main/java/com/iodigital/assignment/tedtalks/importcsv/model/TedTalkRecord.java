package com.iodigital.assignment.tedtalks.importcsv.model;

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
public class TedTalkRecord {

    @NotBlank(message = "Title is required")
    String title;

    @NotBlank(message = "Speaker is required")
    String speaker;

    @NotNull(message = "Date is required")
    LocalDate date;

    @NotNull(message = "Likes count is required")
    @PositiveOrZero(message = "Likes count must be a positive number or zero")
    long views;

    @NotNull(message = "Likes count is required")
    @PositiveOrZero(message = "Likes count must be a positive number or zero")
    long likes;

    @NotBlank(message = "Link is required")
    String link;

    public boolean isEmpty() {
        return title == null || title.isBlank();
    }

}

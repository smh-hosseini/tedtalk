package com.iodigital.assignment.tedtalks.web.exeptionhandler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
@AllArgsConstructor
public class ApiErrorDTO {

    private final HttpStatus status;
    private final String error;
    private final String message;
    private final Map<String, String> fieldErrors;
}

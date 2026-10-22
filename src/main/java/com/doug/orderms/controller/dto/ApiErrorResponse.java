package com.doug.orderms.controller.dto;


import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse (
        Instant timestamp,
        @Schema(example = "400")
        int status,
        String error,
        List<FieldError> details

) {
    public record FieldError(String field, String message){

    }
}

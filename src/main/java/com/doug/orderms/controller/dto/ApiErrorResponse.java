package com.doug.orderms.controller.dto;


import java.time.Instant;
import java.util.List;

public record ApiErrorResponse (
        Instant timestamp,
        int status,
        String error,
        List<FieldError> details

) {
    public record FieldError(String field, String message){

    }
}

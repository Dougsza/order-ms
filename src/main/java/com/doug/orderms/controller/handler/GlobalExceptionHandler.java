package com.doug.orderms.controller.handler;


import com.doug.orderms.controller.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.core.MethodParameter;


import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handlerValidationErrors(
            MethodArgumentNotValidException exception
    ){
        List<ApiErrorResponse.FieldError> details =
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error -> new ApiErrorResponse.FieldError(
                                error.getField(),
                                error.getDefaultMessage()
                        ))
                        .toList();

        var status = HttpStatus.BAD_REQUEST;
        var body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                details
        );

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodValidationErrors(
            HandlerMethodValidationException exception
    ) {
        List<ApiErrorResponse.FieldError> details =
                exception.getParameterValidationResults()
                        .stream()
                        .flatMap(result -> {
                            String field = getParameterName(result.getMethodParameter());

                            return result.getResolvableErrors()
                                    .stream()
                                    .map(error -> new ApiErrorResponse.FieldError(
                                            field,
                                            error.getDefaultMessage()
                                    ));
                        })
                        .toList();

        var status = HttpStatus.BAD_REQUEST;
        var body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                details
        );

        return ResponseEntity.status(status).body(body);
    }

    private String getParameterName(MethodParameter parameter) {
        RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);

        if (requestParam != null && !requestParam.name().isBlank()) {
            return requestParam.name();
        }

        return parameter.getParameterName();
    }
}

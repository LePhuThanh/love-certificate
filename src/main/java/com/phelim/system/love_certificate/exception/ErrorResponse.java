package com.phelim.system.love_certificate.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ErrorResponse {

    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("errorCode")
    private String errorCode;

    @JsonProperty("message")
    private String message;

    @JsonProperty("details")
    private String details;

    @JsonProperty("path")
    private String path;

    @JsonProperty("validationErrors")
    private List<ValidationError> validationErrors;

    @Setter
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        @JsonProperty("field")
        private String field;

        @JsonProperty("message")
        private String message;
    }
}
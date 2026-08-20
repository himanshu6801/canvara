package com.canvara.app.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@Schema(description = "Standard error payload returned for 4xx/5xx responses")
public class ApiErrorResponse {
    @Schema(description = "HTTP status code", example = "404")
    private int                 status;
    @Schema(description = "Short error category", example = "Not Found")
    private String              error;
    @Schema(description = "Human-readable error message", example = "Artwork not found")
    private String              message;
    @Schema(description = "Field-level validation errors, keyed by field name (only populated on 400 validation failures)")
    private Map<String, String> fieldErrors;   // populated on validation failure
    @Schema(description = "When the error occurred")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime       timestamp;
}

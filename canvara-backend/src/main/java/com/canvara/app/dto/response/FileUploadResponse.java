package com.canvara.app.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Result of a successful image upload")
public class FileUploadResponse {
    @Schema(description = "Stored filename; pass this as imageFilename when creating an artwork", example = "a1b2c3.jpg")
    private String filename;   // stored filename  e.g. "a1b2c3.jpg"
    @Schema(description = "Full public URL of the uploaded file", example = "http://localhost:8080/uploads/artworks/a1b2c3.jpg")
    private String url;        // full public URL  e.g. "http://localhost:8080/uploads/artworks/a1b2c3.jpg"
}

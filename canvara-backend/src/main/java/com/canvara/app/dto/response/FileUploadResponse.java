package com.canvara.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileUploadResponse {
    private String filename;   // stored filename  e.g. "a1b2c3.jpg"
    private String url;        // full public URL  e.g. "http://localhost:8080/uploads/artworks/a1b2c3.jpg"
}

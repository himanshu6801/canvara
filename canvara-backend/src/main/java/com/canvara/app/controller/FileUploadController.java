package com.canvara.app.controller;

import com.canvara.app.dto.response.FileUploadResponse;
import com.canvara.app.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final StorageService storageService;

    /**
     * POST /api/upload/artwork
     * Supplier uploads an image before creating an artwork listing.
     *
     * Flow:
     *  1. Frontend sends multipart/form-data with field name "file"
     *  2. This endpoint validates, stores the file, returns filename + URL
     *  3. Frontend passes the returned filename in the subsequent POST /api/artworks body
     *
     * Returns 200 with { filename, url }
     */
    @PostMapping("/artwork")
    @PreAuthorize("hasRole('SUPPLIER')")
    public ResponseEntity<FileUploadResponse> uploadArtworkImage(
            @RequestParam("file") MultipartFile file) {

        String filename = storageService.store(file);
        String url      = storageService.resolveUrl(filename);

        return ResponseEntity.ok(new FileUploadResponse(filename, url));
    }
}

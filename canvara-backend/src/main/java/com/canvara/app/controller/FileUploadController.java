package com.canvara.app.controller;

import com.canvara.app.dto.response.ApiErrorResponse;
import com.canvara.app.dto.response.FileUploadResponse;
import com.canvara.app.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
@Tag(name = "File Upload", description = "Upload images to be attached to artwork listings")
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
    @Operation(
        summary = "Upload an artwork image",
        description = "Supplier uploads an image before creating an artwork listing. Send " +
            "multipart/form-data with field name \"file\". The returned `filename` must be passed " +
            "as `imageFilename` in the subsequent POST /api/artworks request."
    )
    @RequestBody(
        required = true,
        content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "File stored successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid or missing file",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "File storage error",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping(value = "/artwork", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> uploadArtworkImage(
            @RequestParam("file") MultipartFile file) {

        String filename = storageService.store(file);
        String url      = storageService.resolveUrl(filename);

        return ResponseEntity.ok(new FileUploadResponse(filename, url));
    }
}

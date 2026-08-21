package com.canvara.app.controller;

import com.canvara.app.dto.request.ArtworkDeleteRequest;
import com.canvara.app.dto.request.ArtworkStatusRequest;
import com.canvara.app.dto.request.CreateArtworkRequest;
import com.canvara.app.dto.request.UpdateArtworkRequest;
import com.canvara.app.dto.response.ApiErrorResponse;
import com.canvara.app.dto.response.ArtworkDetailResponse;
import com.canvara.app.dto.response.ArtworkSummaryResponse;
import com.canvara.app.enums.Category;
import com.canvara.app.enums.Medium;
import com.canvara.app.enums.Size;
import com.canvara.app.enums.Style;
import com.canvara.app.service.ArtworkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/artworks")
@RequiredArgsConstructor
@Tag(name = "Artworks", description = "Browse, list, and manage artwork listings")
@Slf4j
public class ArtworkController {

    private final ArtworkService artworkService;

    // ── PUBLIC endpoints ──────────────────────────────────────────────────

    /**
     * GET /api/artworks
     * Browse all AVAILABLE artworks.
     * Query params: keyword, category, page, size, sort
     * Example: GET /api/artworks?keyword=ocean&category=SEASCAPE&page=0&size=12
     */
    @Operation(
        summary = "Browse artworks",
        description = "Public, paginated search over artwork listings. Filter by keyword, status, " +
            "category, medium, style, and size."
    )
    @ApiResponse(responseCode = "200", description = "Page of matching artworks")
    @GetMapping
    public ResponseEntity<Page<ArtworkSummaryResponse>> getAll(
            @Parameter(description = "Free-text search across title/description") @RequestParam(required = false) String keyword,
            @Parameter(description = "Filter by artwork status (e.g. AVAILABLE, SOLD, UNLISTED)") @RequestParam(required = false) String status,
            @Parameter(description = "Filter by one or more categories") @RequestParam(required = false) Set<Category> categories,  // was: Category category
            @Parameter(description = "Filter by one or more mediums") @RequestParam(required = false) Set<Medium>   mediums,     // new
            @Parameter(description = "Filter by one or more styles") @RequestParam(required = false) Set<Style>    styles,      // new
            @Parameter(description = "Filter by size") @RequestParam(required = false) Size  artSize,
            @Parameter(description = "Pagination and sorting (default: page=0, size=12, sort=createdAt,desc)")
            @PageableDefault(size = 12, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("GET /artworks  - keyword={}, status={}, categories={}, mediums={}, styles={}, artSize={}, page={}",
                keyword, status, categories, mediums, styles, artSize, pageable);
        return ResponseEntity.ok(
                artworkService.getPublicArtworks(status, keyword, categories, mediums, styles, artSize, pageable)
        );
    }

    /**
     * GET /api/artworks/{id}
     * Get full detail for a single painting (public).
     */
    @Operation(summary = "Get artwork by ID", description = "Public detail view of a single artwork listing.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Artwork found"),
        @ApiResponse(responseCode = "404", description = "Artwork not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ArtworkDetailResponse> getById(
            @Parameter(description = "Artwork ID") @PathVariable Long id) {
        log.info("GET /artworks/{}", id);
        return ResponseEntity.ok(artworkService.getById(id));
    }

    // ── SUPPLIER-ONLY endpoints ───────────────────────────────────────────

    /**
     * GET /api/artworks/my
     * Logged-in supplier: all their artworks (all statuses).
     */
    @Operation(
        summary = "List my artworks",
        description = "Supplier-only: returns all artworks owned by the caller, in every status."
    )
    @ApiResponse(responseCode = "200", description = "List of the supplier's artworks")
    @GetMapping("/my")
    public ResponseEntity<List<ArtworkSummaryResponse>> getMine(Principal principal) {
        log.info("GET /artworks/my - supplier={}", principal.getName());
        return ResponseEntity.ok(artworkService.getMyArtworks(principal.getName()));
    }

    /**
     * GET /api/artworks/my/{id}
     * Supplier: full detail view of one of their own artworks (includes pending request count).
     */
    @Operation(
        summary = "Get my artwork by ID",
        description = "Supplier-only: full detail view of one of the caller's own artworks, " +
            "including the pending purchase-request count."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Artwork found"),
        @ApiResponse(responseCode = "403", description = "Artwork does not belong to the caller",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Artwork not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/my/{id}")
    public ResponseEntity<ArtworkDetailResponse> getMyById(
            @Parameter(description = "Artwork ID") @PathVariable Long id, Principal principal) {

        log.info("GET /artworks/my/{} - supplier={}", id, principal.getName());
        return ResponseEntity.ok(artworkService.getMyArtworkById(id, principal.getName()));
    }

    /**
     * POST /api/artworks
     * Supplier creates a new listing.
     * The imageFilename field must come from a prior /api/upload call.
     */
    @Operation(
        summary = "Create an artwork listing",
        description = "Supplier creates a new artwork listing. `imageFilename` must come from a prior " +
            "POST /api/upload/artwork call."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Artwork created"),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ArtworkDetailResponse> create(
            @Valid @RequestBody CreateArtworkRequest req) {

        log.info("POST /artworks - supplier={}", req.getSupplierEmail());
        ArtworkDetailResponse created = artworkService.create(req, req.getSupplierEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/artworks/{id}
     * Supplier updates text/pricing fields of their own artwork.
     */
    @Operation(
        summary = "Update an artwork listing",
        description = "Supplier updates text/pricing fields of one of their own artworks."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Artwork updated"),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Artwork does not belong to the caller",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Artwork not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ArtworkDetailResponse> update(
            @Parameter(description = "Artwork ID") @PathVariable Long id,
            @Valid @RequestBody UpdateArtworkRequest req) {

        log.info("PUT /artworks/{} - supplier={}", id, req.getSupplierEmail());
        return ResponseEntity.ok(artworkService.update(id, req, req.getSupplierEmail()));
    }

    /**
     * PATCH /api/artworks/{id}/status
     * Supplier changes artwork status: AVAILABLE → SOLD | UNLISTED.
     * Marking SOLD auto-cancels all pending purchase requests.
     */
    @Operation(
        summary = "Change artwork status",
        description = "Supplier changes an artwork's status (e.g. AVAILABLE → SOLD | UNLISTED). " +
            "Marking an artwork SOLD auto-cancels all pending purchase requests."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status updated"),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Artwork does not belong to the caller",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Artwork not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<ArtworkDetailResponse> updateStatus(
            @Parameter(description = "Artwork ID") @PathVariable Long id,
            @Valid @RequestBody ArtworkStatusRequest req) {

        log.info("PATCH /artworks/{}/status - supplier={}, newStatus={}", id, req.getSupplierEmail(), req.getStatus());
        return ResponseEntity.ok(artworkService.updateStatus(id, req, req.getSupplierEmail()));
    }

    /**
     * DELETE /api/artworks/{id}
     * Supplier deletes their own artwork (also removes image from disk).
     */
    @Operation(
        summary = "Delete an artwork listing",
        description = "Supplier deletes one of their own artworks. Also removes the associated image."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Artwork deleted"),
        @ApiResponse(responseCode = "403", description = "Artwork does not belong to the caller",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Artwork not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Artwork ID") @PathVariable Long id,
            @Valid @RequestBody ArtworkDeleteRequest artworkDeleteRequest) {

        log.info("DELETE /artworks/{} - supplier={}", id, artworkDeleteRequest.getSupplierEmail());
        artworkService.delete(id, artworkDeleteRequest.getSupplierEmail());
        return ResponseEntity.noContent().build();
    }
}

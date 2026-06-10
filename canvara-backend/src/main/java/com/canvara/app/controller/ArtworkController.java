package com.canvara.app.controller;

import com.canvara.app.dto.request.ArtworkDeleteRequest;
import com.canvara.app.dto.request.ArtworkStatusRequest;
import com.canvara.app.dto.request.CreateArtworkRequest;
import com.canvara.app.dto.request.UpdateArtworkRequest;
import com.canvara.app.dto.response.ArtworkDetailResponse;
import com.canvara.app.dto.response.ArtworkSummaryResponse;
import com.canvara.app.enums.Category;
import com.canvara.app.enums.Medium;
import com.canvara.app.enums.Size;
import com.canvara.app.enums.Style;
import com.canvara.app.service.ArtworkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/artworks")
@RequiredArgsConstructor
public class ArtworkController {

    private final ArtworkService artworkService;

    // ── PUBLIC endpoints ──────────────────────────────────────────────────

    /**
     * GET /api/artworks
     * Browse all AVAILABLE artworks.
     * Query params: keyword, category, page, size, sort
     * Example: GET /api/artworks?keyword=ocean&category=SEASCAPE&page=0&size=12
     */
    @GetMapping
    public ResponseEntity<Page<ArtworkSummaryResponse>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Set<Category> categories,  // was: Category category
            @RequestParam(required = false) Set<Medium>   mediums,     // new
            @RequestParam(required = false) Set<Style>    styles,      // new
            @RequestParam(required = false) Size  artSize,
            @PageableDefault(size = 12, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(
                artworkService.getPublicArtworks(status, keyword, categories, mediums, styles, artSize, pageable)
        );
    }

    /**
     * GET /api/artworks/{id}
     * Get full detail for a single painting (public).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ArtworkDetailResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(artworkService.getById(id));
    }

    // ── SUPPLIER-ONLY endpoints ───────────────────────────────────────────

    /**
     * GET /api/artworks/my
     * Logged-in supplier: all their artworks (all statuses).
     */
    @GetMapping("/my")
    public ResponseEntity<List<ArtworkSummaryResponse>> getMine(Principal principal) {
        return ResponseEntity.ok(artworkService.getMyArtworks(principal.getName()));
    }

    /**
     * GET /api/artworks/my/{id}
     * Supplier: full detail view of one of their own artworks (includes pending request count).
     */
    @GetMapping("/my/{id}")
    public ResponseEntity<ArtworkDetailResponse> getMyById(
            @PathVariable Long id, Principal principal) {

        return ResponseEntity.ok(artworkService.getMyArtworkById(id, principal.getName()));
    }

    /**
     * POST /api/artworks
     * Supplier creates a new listing.
     * The imageFilename field must come from a prior /api/upload call.
     */
    @PostMapping
    public ResponseEntity<ArtworkDetailResponse> create(
            @Valid @RequestBody CreateArtworkRequest req) {

        ArtworkDetailResponse created = artworkService.create(req, req.getSupplierEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/artworks/{id}
     * Supplier updates text/pricing fields of their own artwork.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ArtworkDetailResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateArtworkRequest req) {

        return ResponseEntity.ok(artworkService.update(id, req, req.getSupplierEmail()));
    }

    /**
     * PATCH /api/artworks/{id}/status
     * Supplier changes artwork status: AVAILABLE → SOLD | UNLISTED.
     * Marking SOLD auto-cancels all pending purchase requests.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ArtworkDetailResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ArtworkStatusRequest req) {

        return ResponseEntity.ok(artworkService.updateStatus(id, req, req.getSupplierEmail()));
    }

    /**
     * DELETE /api/artworks/{id}
     * Supplier deletes their own artwork (also removes image from disk).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @Valid @RequestBody ArtworkDeleteRequest artworkDeleteRequest) {

        artworkService.delete(id, artworkDeleteRequest.getSupplierEmail());
        return ResponseEntity.noContent().build();
    }
}

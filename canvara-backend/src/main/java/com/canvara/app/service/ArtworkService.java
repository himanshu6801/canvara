package com.canvara.app.service;

import com.canvara.app.dto.request.ArtworkStatusRequest;
import com.canvara.app.dto.request.CreateArtworkRequest;
import com.canvara.app.dto.request.UpdateArtworkRequest;
import com.canvara.app.dto.response.ArtworkDetailResponse;
import com.canvara.app.dto.response.ArtworkSummaryResponse;
import com.canvara.app.entity.Artwork;
import com.canvara.app.entity.User;
import com.canvara.app.enums.ArtworkStatus;
import com.canvara.app.enums.Category;
import com.canvara.app.enums.Medium;
import com.canvara.app.exception.ResourceNotFoundException;
import com.canvara.app.exception.UnauthorizedAccessException;
import com.canvara.app.repository.ArtworkRepository;
import com.canvara.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.canvara.app.util.EnumUtils.getEnumOrDefault;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArtworkService {

    private final ArtworkRepository        artworkRepository;
    private final UserRepository           userRepository;
    private final StorageService           storageService;

    // ── PUBLIC ───────────────────────────────────────────────────────────

    /** Paginated gallery: only AVAILABLE artworks, optional filters. */
    public Page<ArtworkSummaryResponse> getPublicArtworks(
            String status, String keyword, Category category, Pageable pageable) {

        return artworkRepository
            .findAllPublic(status, category, keyword, pageable)
            .map(this::toSummary);
    }

    /** Single artwork detail — visible to anyone. */
    public ArtworkDetailResponse getById(Long id) {
        Artwork artwork = findOrThrow(id);
        return toDetail(artwork, false);
    }

    // ── SUPPLIER ─────────────────────────────────────────────────────────

    /** All artworks for the logged-in supplier (all statuses). */
    public List<ArtworkSummaryResponse> getMyArtworks(String supplierEmail) {
        User supplier = findUserOrThrow(supplierEmail);
        return artworkRepository.findBySupplierId(supplier.getId())
            .stream()
            .map(this::toSummary)
            .collect(Collectors.toList());
    }

    /** Supplier detail view — includes pending request count. */
    public ArtworkDetailResponse getMyArtworkById(Long id, String supplierEmail) {
        User supplier = findUserOrThrow(supplierEmail);
        Artwork artwork = artworkRepository.findByIdAndSupplierId(id, supplier.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Artwork", id));
        return toDetail(artwork, true);
    }

    /** Create a new listing. The image must already be uploaded. */
    @Transactional
    public ArtworkDetailResponse create(CreateArtworkRequest req, String supplierEmail) {
        User supplier = findUserOrThrow(supplierEmail);

        Artwork artwork = Artwork.builder()
            .title(req.getTitle())
            .description(req.getDescription())
            .price(req.getPrice())
            .medium(req.getMedium().name())
            .category(req.getCategory().name())
            .dimensions(req.getDimensions())
            .imageFilename(req.getImageFilename())
            .status(ArtworkStatus.AVAILABLE.name())
            .supplier(supplier)
            .build();

        return toDetail(artworkRepository.save(artwork), false);
    }

    /** Update editable fields. Image is NOT changed here — use /upload endpoint. */
    @Transactional
    public ArtworkDetailResponse update(Long id, UpdateArtworkRequest req, String supplierEmail) {
        Artwork artwork = findOwnedOrThrow(id, supplierEmail);

        artwork.setTitle(req.getTitle());
        artwork.setDescription(req.getDescription());
        artwork.setPrice(req.getPrice());
        artwork.setMedium(req.getMedium().name());
        artwork.setCategory(req.getCategory().name());
        artwork.setDimensions(req.getDimensions());

        return toDetail(artworkRepository.save(artwork), true);
    }

    /**
     * Change artwork status.
     * When marked SOLD: auto-cancel all remaining PENDING purchase requests.
     */
    @Transactional
    public ArtworkDetailResponse updateStatus(Long id, ArtworkStatusRequest req, String supplierEmail) {
        Artwork artwork = findOwnedOrThrow(id, supplierEmail);
        artwork.setStatus(req.getStatus().name());

        return toDetail(artworkRepository.save(artwork), true);
    }

    /**
     * Delete artwork.
     * Also removes the stored image file from disk.
     */
    @Transactional
    public void delete(Long id, String supplierEmail) {
        Artwork artwork = findOwnedOrThrow(id, supplierEmail);
        storageService.delete(artwork.getImageFilename());
        artworkRepository.delete(artwork);
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private Artwork findOrThrow(Long id) {
        return artworkRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Artwork", id));
    }

    private Artwork findOwnedOrThrow(Long id, String supplierEmail) {
        User supplier = findUserOrThrow(supplierEmail);
        return artworkRepository.findByIdAndSupplierId(id, supplier.getId())
            .orElseThrow(() -> new UnauthorizedAccessException(
                "You do not have permission to modify artwork with id: " + id));
    }

    private User findUserOrThrow(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    // ── Mapping ───────────────────────────────────────────────────────────

    private ArtworkSummaryResponse toSummary(Artwork a) {
        return ArtworkSummaryResponse.builder()
            .id(a.getId())
            .title(a.getTitle())
            .price(a.getPrice())
            .medium(getEnumOrDefault(Medium.class, a.getCategory(), Medium.OTHER))
            .category(getEnumOrDefault(Category.class, a.getCategory(), Category.OTHER))
            .dimensions(a.getDimensions())
            .imageUrl(storageService.resolveUrl(a.getImageFilename()))
            .status(getEnumOrDefault(ArtworkStatus.class, a.getCategory(), ArtworkStatus.AVAILABLE))
            .supplierName(a.getSupplier().getFullName())
            .createdAt(a.getCreatedAt())
            .build();
    }

    private ArtworkDetailResponse toDetail(Artwork a, boolean includePendingCount) {

        return ArtworkDetailResponse.builder()
            .id(a.getId())
            .title(a.getTitle())
            .description(a.getDescription())
            .price(a.getPrice())
            .medium(Medium.valueOf(a.getMedium()))
            .category(Category.valueOf(a.getCategory()))
            .dimensions(a.getDimensions())
            .imageUrl(storageService.resolveUrl(a.getImageFilename()))
            .status(ArtworkStatus.valueOf(a.getStatus()))
            .supplierId(a.getSupplier().getId())
            .supplierName(a.getSupplier().getFullName())
            .supplierEmail(a.getSupplier().getEmail())
            .supplierBio(a.getSupplier().getBio())
            .supplierProfileImageUrl(a.getSupplier().getProfileImageUrl())
            .createdAt(a.getCreatedAt())
            .updatedAt(a.getUpdatedAt())
            .build();
    }
}

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
import com.canvara.app.exception.ResourceNotFoundException;
import com.canvara.app.exception.UnauthorizedAccessException;
import com.canvara.app.repository.ArtworkRepository;
import com.canvara.app.repository.PurchaseRequestRepository;
import com.canvara.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArtworkService {

    private final ArtworkRepository        artworkRepository;
    private final PurchaseRequestRepository purchaseRequestRepository;
    private final UserRepository           userRepository;
    private final StorageService           storageService;

    // ── PUBLIC ───────────────────────────────────────────────────────────

    /** Paginated gallery: only AVAILABLE artworks, optional filters. */
    public Page<ArtworkSummaryResponse> getPublicArtworks(
            String keyword, Category category, Pageable pageable) {

        return artworkRepository
            .findAllPublic(ArtworkStatus.AVAILABLE, category, keyword, pageable)
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
            .medium(req.getMedium())
            .category(req.getCategory())
            .dimensions(req.getDimensions())
            .imageFilename(req.getImageFilename())
            .status(ArtworkStatus.AVAILABLE)
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
        artwork.setMedium(req.getMedium());
        artwork.setCategory(req.getCategory());
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
        artwork.setStatus(req.getStatus());

        if (req.getStatus() == ArtworkStatus.SOLD) {
            int cancelled = purchaseRequestRepository.cancelPendingForArtwork(id);
            System.out.printf("Auto-cancelled %d pending requests for artwork %d%n", cancelled, id);
        }

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
            .medium(a.getMedium())
            .category(a.getCategory())
            .dimensions(a.getDimensions())
            .imageUrl(storageService.resolveUrl(a.getImageFilename()))
            .status(a.getStatus())
            .supplierName(a.getSupplier().getFullName())
            .createdAt(a.getCreatedAt())
            .build();
    }

    private ArtworkDetailResponse toDetail(Artwork a, boolean includePendingCount) {
        int pendingCount = includePendingCount
            ? (int) purchaseRequestRepository.countByArtworkIdAndStatus(
                a.getId(), com.canvara.app.enums.PurchaseRequestStatus.PENDING)
            : 0;

        return ArtworkDetailResponse.builder()
            .id(a.getId())
            .title(a.getTitle())
            .description(a.getDescription())
            .price(a.getPrice())
            .medium(a.getMedium())
            .category(a.getCategory())
            .dimensions(a.getDimensions())
            .imageUrl(storageService.resolveUrl(a.getImageFilename()))
            .status(a.getStatus())
            .supplierId(a.getSupplier().getId())
            .supplierName(a.getSupplier().getFullName())
            .supplierBio(a.getSupplier().getBio())
            .supplierProfileImageUrl(a.getSupplier().getProfileImageUrl())
            .pendingRequestCount(pendingCount)
            .createdAt(a.getCreatedAt())
            .updatedAt(a.getUpdatedAt())
            .build();
    }
}

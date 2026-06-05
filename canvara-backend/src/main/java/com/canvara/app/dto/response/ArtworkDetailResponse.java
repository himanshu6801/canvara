package com.canvara.app.dto.response;

import com.canvara.app.enums.ArtworkStatus;
import com.canvara.app.enums.Category;
import com.canvara.app.enums.Medium;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Full DTO for the artwork detail page.
 * Includes supplier info and purchase request count.
 */
@Data
@Builder
public class ArtworkDetailResponse {
    private Long          id;
    private String        title;
    private String        description;
    private BigDecimal    price;
    private Medium        medium;
    private Category      category;
    private String        dimensions;
    private String        imageUrl;
    private ArtworkStatus status;

    // Supplier info (public-safe fields only)
    private Long          supplierId;
    private String        supplierName;
    private String        supplierBio;
    private String        supplierProfileImageUrl;

    private int           pendingRequestCount;   // visible to supplier only
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

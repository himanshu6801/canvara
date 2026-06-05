package com.canvara.app.dto.response;

import com.canvara.app.enums.ArtworkStatus;
import com.canvara.app.enums.Category;
import com.canvara.app.enums.Medium;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lightweight DTO for gallery grid listings.
 * Does NOT expose supplier details or purchase request data.
 */
@Data
@Builder
public class ArtworkSummaryResponse {
    private Long          id;
    private String        title;
    private BigDecimal    price;
    private Medium        medium;
    private Category      category;
    private String        dimensions;
    private String        imageUrl;        // full resolved URL
    private ArtworkStatus status;
    private String        supplierName;
    private LocalDateTime createdAt;
}

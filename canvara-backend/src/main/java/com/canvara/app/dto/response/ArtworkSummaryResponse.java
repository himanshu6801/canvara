package com.canvara.app.dto.response;

import com.canvara.app.enums.ArtworkStatus;
import com.canvara.app.enums.Category;
import com.canvara.app.enums.Medium;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}

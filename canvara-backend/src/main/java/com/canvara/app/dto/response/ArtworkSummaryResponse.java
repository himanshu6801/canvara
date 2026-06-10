package com.canvara.app.dto.response;

import com.canvara.app.enums.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

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
    private String        dimensions;
    private Size          size;
    private String        imageUrl;        // full resolved URL
    private ArtworkStatus status;
    private String        supplierName;
    private String        supplierEmail;
    private Set<Category> categories;
    private Set<Medium> mediums;
    private Set<Style>    styles;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}

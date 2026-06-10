package com.canvara.app.dto.response;

import com.canvara.app.enums.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

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
    private Set<Medium>   medium;
    private Set<Category> category;
    private Set<Style>    styles;
    private Size          size;
    private String        dimensions;
    private String        imageUrl;
    private String        storyTitle;
    private String        storyType;
    private String        storyContent;
    private ArtworkStatus status;

    // Supplier info (public-safe fields only)
    private Long          supplierId;
    private String        supplierName;
    private String        supplierEmail;
    private String        supplierBio;
    private String        supplierProfileImageUrl;

    private int           pendingRequestCount;   // visible to supplier only
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}

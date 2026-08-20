package com.canvara.app.dto.request;

import com.canvara.app.enums.Category;
import com.canvara.app.enums.Medium;
import com.canvara.app.enums.Style;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class UpdateArtworkRequest {

    @Schema(description = "Artwork title", example = "Sunset Over the Bay")
    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    @Schema(description = "Artwork description", example = "Oil on canvas, painted en plein air.")
    @Size(max = 2000)
    private String description;

    @Schema(description = "Listing price", example = "450.00")
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal price;

    @Schema(description = "Mediums used")
    @NotNull(message = "Medium is required")
    private Set<Medium> medium;

    @Schema(description = "Categories the artwork belongs to")
    @NotNull(message = "Category is required")
    private Set<Category> category;

    @Schema(description = "Styles the artwork belongs to")
    @NotNull(message = "styles is required")
    private Set<Style> styles;

    @Schema(description = "Physical dimensions", example = "24x36 in")
    @Size(max = 100)
    private String dimensions;

    @Schema(description = "Email of the supplier who owns the listing", example = "artist@example.com")
    @NotBlank(message = "Supplier email is required")
    private String supplierEmail;
}

package com.canvara.app.dto.request;

import com.canvara.app.enums.Category;
import com.canvara.app.enums.Medium;
import com.canvara.app.enums.Style;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
public class CreateArtworkRequest {

    @Schema(description = "Artwork title", example = "Sunset Over the Bay")
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Schema(description = "Artwork description", example = "Oil on canvas, painted en plein air.")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Schema(description = "Listing price", example = "450.00")
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    @Digits(integer = 8, fraction = 2, message = "Invalid price format")
    private BigDecimal price;

//    @NotNull(message = "Medium is required")
    @Schema(description = "Mediums used")
    private Set<Medium> mediums;

//    @NotNull(message = "Category is required")
    @Schema(description = "Categories the artwork belongs to")
    private Set<Category> categories;

//    @NotNull(message = "styles is required")
    @Schema(description = "Styles the artwork belongs to")
    private Set<Style> styles;

    @Schema(description = "Physical dimensions", example = "24x36 in")
    @Size(max = 100, message = "Dimensions must not exceed 100 characters")
    private String dimensions;

    @Schema(description = "Filename returned by a prior POST /api/upload/artwork call", example = "a1b2c3.jpg")
    @NotBlank(message = "Image filename is required")
    private String imageFilename;   // returned by /api/upload after file is saved

    @Schema(description = "Email of the supplier creating the listing", example = "artist@example.com")
    @NotBlank(message = "Supplier email is required")
    private String supplierEmail;
}

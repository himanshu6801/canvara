package com.canvara.app.dto.request;

import com.canvara.app.enums.Category;
import com.canvara.app.enums.Medium;
import com.canvara.app.enums.Style;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
public class CreateArtworkRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    @Digits(integer = 8, fraction = 2, message = "Invalid price format")
    private BigDecimal price;

    @NotNull(message = "Medium is required")
    private Set<Medium> mediums;

    @NotNull(message = "Category is required")
    private Set<Category> categories;

    @NotNull(message = "styles is required")
    private Set<Style> styles;

    @Size(max = 100, message = "Dimensions must not exceed 100 characters")
    private String dimensions;

    @NotBlank(message = "Image filename is required")
    private String imageFilename;   // returned by /api/upload after file is saved

    @NotBlank(message = "Supplier email is required")
    private String supplierEmail;
}

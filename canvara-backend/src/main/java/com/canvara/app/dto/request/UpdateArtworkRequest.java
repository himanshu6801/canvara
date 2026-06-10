package com.canvara.app.dto.request;

import com.canvara.app.enums.Category;
import com.canvara.app.enums.Medium;
import com.canvara.app.enums.Style;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class UpdateArtworkRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    @Size(max = 2000)
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal price;

    @NotNull(message = "Medium is required")
    private Set<Medium> medium;

    @NotNull(message = "Category is required")
    private Set<Category> category;

    @NotNull(message = "styles is required")
    private Set<Style> styles;

    @Size(max = 100)
    private String dimensions;

    @NotBlank(message = "Supplier email is required")
    private String supplierEmail;
}

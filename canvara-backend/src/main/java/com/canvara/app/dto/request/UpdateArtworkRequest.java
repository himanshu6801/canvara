package com.canvara.app.dto.request;

import com.canvara.app.enums.Category;
import com.canvara.app.enums.Medium;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

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

    @NotNull
    private Medium medium;

    @NotNull
    private Category category;

    @Size(max = 100)
    private String dimensions;

    @NotBlank(message = "Supplier email is required")
    private String supplierEmail;
}

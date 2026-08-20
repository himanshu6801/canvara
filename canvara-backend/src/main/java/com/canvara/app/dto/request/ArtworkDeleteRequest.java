package com.canvara.app.dto.request;

import com.canvara.app.enums.ArtworkStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArtworkDeleteRequest {

    @Schema(description = "Email of the supplier who owns the listing", example = "artist@example.com")
    @NotBlank(message = "Supplier email is required")
    private String supplierEmail;
}

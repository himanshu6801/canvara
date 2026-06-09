package com.canvara.app.dto.request;

import com.canvara.app.enums.ArtworkStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArtworkDeleteRequest {

    @NotBlank(message = "Supplier email is required")
    private String supplierEmail;
}

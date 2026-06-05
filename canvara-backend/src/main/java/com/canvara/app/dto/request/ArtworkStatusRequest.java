package com.canvara.app.dto.request;

import com.canvara.app.enums.ArtworkStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArtworkStatusRequest {

    @NotNull(message = "Status is required")
    private ArtworkStatus status;
}

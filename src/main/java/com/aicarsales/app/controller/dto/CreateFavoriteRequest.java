package com.aicarsales.app.controller.dto;

import jakarta.validation.constraints.NotNull;

public record CreateFavoriteRequest(
        @NotNull Long userId,
        @NotNull Long carId,
        String note
) {
}

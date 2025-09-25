package com.aicarsales.app.controller.dto;

import java.time.OffsetDateTime;

public record FavoriteResponse(
        Long id,
        Long carId,
        String carModelName,
        String carTrim,
        Integer carPrice,
        String note,
        OffsetDateTime createdAt
) {
}

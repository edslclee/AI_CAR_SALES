package com.aicarsales.app.controller.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record RecommendationResponse(
        Long id,
        OffsetDateTime generatedAt,
        String rationale,
        String scoringWeights,
        List<RecommendationItemResponse> items
) {

    public record RecommendationItemResponse(
            Integer rank,
            Double score,
            String scoreBreakdown,
            CarSummary car
    ) {
    }

    public record CarSummary(
            Long id,
            String modelName,
            String trim,
            Integer price,
            String bodyType,
            String fuelType
    ) {
    }
}

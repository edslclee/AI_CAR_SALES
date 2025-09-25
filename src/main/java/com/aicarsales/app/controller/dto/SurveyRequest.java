package com.aicarsales.app.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SurveyRequest(
        @NotNull Long userId,
        Integer budgetMin,
        Integer budgetMax,
        String usage,
        Short passengers,
        List<String> preferredBodyTypes,
        List<String> preferredBrands,
        Short yearRangeStart,
        Short yearRangeEnd,
        Integer mileageRangeStart,
        Integer mileageRangeEnd,
        Map<String, Object> options
) {
}

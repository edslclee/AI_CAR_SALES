package com.aicarsales.app.controller;

import com.aicarsales.app.controller.dto.RecommendationResponse;
import com.aicarsales.app.controller.dto.RecommendationResponse.CarSummary;
import com.aicarsales.app.controller.dto.RecommendationResponse.RecommendationItemResponse;
import com.aicarsales.app.domain.Recommendation;
import com.aicarsales.app.service.RecommendationService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecommendationResponse> findRecommendation(@PathVariable Long id) {
        Recommendation recommendation = recommendationService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recommendation not found"));

        RecommendationResponse response = new RecommendationResponse(
                recommendation.getId(),
                recommendation.getGeneratedAt(),
                recommendation.getRationale(),
                recommendation.getScoringWeights(),
                mapItems(recommendation));
        return ResponseEntity.ok(response);
    }

    private List<RecommendationItemResponse> mapItems(Recommendation recommendation) {
        return recommendation.getItems().stream()
                .sorted((a, b) -> Integer.compare(a.getRank(), b.getRank()))
                .map(item -> new RecommendationItemResponse(
                        item.getRank(),
                        item.getScore(),
                        item.getScoreBreakdown(),
                        new CarSummary(
                                item.getCar().getId(),
                                item.getCar().getModelName(),
                                item.getCar().getTrim(),
                                item.getCar().getPrice(),
                                item.getCar().getBodyType(),
                                item.getCar().getFuelType()
                        ))
                ).collect(Collectors.toList());
    }
}

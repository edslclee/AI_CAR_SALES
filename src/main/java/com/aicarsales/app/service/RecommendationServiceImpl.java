package com.aicarsales.app.service;

import com.aicarsales.app.domain.Car;
import com.aicarsales.app.domain.Preference;
import com.aicarsales.app.domain.Recommendation;
import com.aicarsales.app.domain.RecommendationItem;
import com.aicarsales.app.domain.User;
import com.aicarsales.app.repository.CarRepository;
import com.aicarsales.app.repository.RecommendationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final CarRepository carRepository;
    private final RecommendationRepository recommendationRepository;

    public RecommendationServiceImpl(CarRepository carRepository, RecommendationRepository recommendationRepository) {
        this.carRepository = carRepository;
        this.recommendationRepository = recommendationRepository;
    }

    @Override
    @Transactional
    public Recommendation createRecommendation(User user, Preference preference) {
        List<Car> candidates = carRepository.findAll();
        if (candidates.isEmpty()) {
            Recommendation empty = new Recommendation();
            empty.setUser(user);
            empty.setRationale("No cars available for recommendation.");
            empty.setScoringWeights(defaultScoringWeightsJson());
            return recommendationRepository.save(empty);
        }

        double targetBudget = deriveTargetBudget(preference);
        String preferredBodyType = preference.getPreferredBodyTypes().isEmpty() ? null : preference.getPreferredBodyTypes().get(0);

        List<RecommendationItem> rankedItems = candidates.stream()
                .map(car -> buildItem(car, targetBudget, preferredBodyType))
                .sorted(Comparator.comparing(RecommendationItem::getScore).reversed())
                .limit(5)
                .collect(Collectors.toList());

        Recommendation recommendation = new Recommendation();
        recommendation.setUser(user);
        recommendation.setRationale("Stubbed recommendation based on budget and body type preference.");
        recommendation.setScoringWeights(defaultScoringWeightsJson());

        int[] rankHolder = {1};
        rankedItems.forEach(item -> {
            item.setRank(rankHolder[0]++);
            recommendation.addItem(item);
        });

        return recommendationRepository.save(recommendation);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Recommendation> findById(Long id) {
        return recommendationRepository.findById(id);
    }

    private RecommendationItem buildItem(Car car, double targetBudget, String preferredBodyType) {
        double budgetScore = computeBudgetScore(targetBudget, car.getPrice());
        double bodyTypeScore = computeBodyTypeScore(preferredBodyType, car.getBodyType());
        double aggregateScore = Math.round((0.7 * budgetScore + 0.3 * bodyTypeScore) * 100.0) / 100.0;

        RecommendationItem item = new RecommendationItem();
        item.setCar(car);
        item.setScore(aggregateScore);
        item.setScoreBreakdown(buildScoreBreakdownJson(budgetScore, bodyTypeScore));
        return item;
    }

    private double computeBudgetScore(double targetBudget, Integer price) {
        if (targetBudget <= 0 || price == null) {
            return 60.0;
        }
        double diff = Math.abs(targetBudget - price);
        double penalty = Math.min(diff / Math.max(targetBudget, 1) * 100, 80);
        return Math.max(100 - penalty, 20);
    }

    private double computeBodyTypeScore(String preferredBodyType, String carBodyType) {
        if (preferredBodyType == null || carBodyType == null) {
            return 50.0;
        }
        return preferredBodyType.equalsIgnoreCase(carBodyType) ? 90.0 : 40.0;
    }

    private String buildScoreBreakdownJson(double budgetScore, double bodyTypeScore) {
        try {
            return OBJECT_MAPPER.writeValueAsString(new ScoreBreakdown(budgetScore, bodyTypeScore));
        } catch (JsonProcessingException e) {
            return "{\"budget\":" + budgetScore + ",\"bodyType\":" + bodyTypeScore + "}";
        }
    }

    private String defaultScoringWeightsJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(new ScoringWeights(0.7, 0.3));
        } catch (JsonProcessingException e) {
            return "{\"budget\":0.7,\"bodyType\":0.3}";
        }
    }

    private double deriveTargetBudget(Preference preference) {
        Integer min = preference.getBudgetMin();
        Integer max = preference.getBudgetMax();
        if (min != null && max != null) {
            return (min + max) / 2.0;
        }
        if (max != null) {
            return max;
        }
        if (min != null) {
            return min;
        }
        return 0.0;
    }

    private record ScoreBreakdown(double budget, double bodyType) {}

    private record ScoringWeights(double budget, double bodyType) {}
}

package com.aicarsales.app.controller;

import com.aicarsales.app.controller.dto.SurveyRequest;
import com.aicarsales.app.controller.dto.SurveyResponse;
import com.aicarsales.app.domain.Preference;
import com.aicarsales.app.domain.Recommendation;
import com.aicarsales.app.domain.User;
import com.aicarsales.app.service.PreferenceService;
import com.aicarsales.app.service.RecommendationService;
import com.aicarsales.app.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/surveys")
public class SurveyController {

    private final UserService userService;
    private final PreferenceService preferenceService;
    private final RecommendationService recommendationService;
    private final ObjectMapper objectMapper;

    public SurveyController(UserService userService,
                            PreferenceService preferenceService,
                            RecommendationService recommendationService,
                            ObjectMapper objectMapper) {
        this.userService = userService;
        this.preferenceService = preferenceService;
        this.recommendationService = recommendationService;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseEntity<SurveyResponse> submitSurvey(@Valid @RequestBody SurveyRequest request) {
        User user = userService.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Preference preference = mapToPreference(request, user);
        Preference savedPreference = preferenceService.save(preference);

        Recommendation recommendation = recommendationService.createRecommendation(user, savedPreference);
        return ResponseEntity.status(HttpStatus.CREATED).body(new SurveyResponse(recommendation.getId()));
    }

    private Preference mapToPreference(SurveyRequest request, User user) {
        Preference preference = new Preference();
        preference.setUser(user);
        preference.setBudgetMin(request.budgetMin());
        preference.setBudgetMax(request.budgetMax());
        preference.setUsage(request.usage());
        preference.setPassengers(request.passengers());
        preference.setPreferredBodyTypes(defaultList(request.preferredBodyTypes()));
        preference.setPreferredBrands(defaultList(request.preferredBrands()));
        preference.setYearRangeStart(request.yearRangeStart());
        preference.setYearRangeEnd(request.yearRangeEnd());
        preference.setMileageRangeStart(request.mileageRangeStart());
        preference.setMileageRangeEnd(request.mileageRangeEnd());
        preference.setOptions(writeOptions(request.options()));
        return preference;
    }

    private List<String> defaultList(List<String> values) {
        return values == null ? List.of() : values;
    }

    private String writeOptions(Map<String, Object> options) {
        if (options == null || options.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(options);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid options payload", e);
        }
    }
}

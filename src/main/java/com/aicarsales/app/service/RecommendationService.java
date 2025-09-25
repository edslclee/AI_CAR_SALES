package com.aicarsales.app.service;

import com.aicarsales.app.domain.Preference;
import com.aicarsales.app.domain.Recommendation;
import com.aicarsales.app.domain.User;
import java.util.Optional;

public interface RecommendationService {

    Recommendation createRecommendation(User user, Preference preference);

    Optional<Recommendation> findById(Long id);
}

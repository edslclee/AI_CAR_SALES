package com.aicarsales.app.repository;

import com.aicarsales.app.domain.Recommendation;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    @Override
    @EntityGraph(attributePaths = {"items", "items.car"})
    Optional<Recommendation> findById(Long id);
}

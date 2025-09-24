package com.aicarsales.app.repository;

import com.aicarsales.app.domain.Preference;
import com.aicarsales.app.domain.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreferenceRepository extends JpaRepository<Preference, Long> {
    List<Preference> findByUser(User user);
}

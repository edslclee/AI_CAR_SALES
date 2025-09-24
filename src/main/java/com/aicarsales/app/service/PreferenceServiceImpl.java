package com.aicarsales.app.service;

import com.aicarsales.app.domain.Preference;
import com.aicarsales.app.domain.User;
import com.aicarsales.app.repository.PreferenceRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PreferenceServiceImpl implements PreferenceService {

    private final PreferenceRepository preferenceRepository;

    public PreferenceServiceImpl(PreferenceRepository preferenceRepository) {
        this.preferenceRepository = preferenceRepository;
    }

    @Override
    @Transactional
    public Preference save(Preference preference) {
        return preferenceRepository.save(preference);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Preference> findByUser(User user) {
        return preferenceRepository.findByUser(user);
    }
}

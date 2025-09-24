package com.aicarsales.app.service;

import com.aicarsales.app.domain.Preference;
import com.aicarsales.app.domain.User;
import java.util.List;

public interface PreferenceService {
    Preference save(Preference preference);
    List<Preference> findByUser(User user);
}

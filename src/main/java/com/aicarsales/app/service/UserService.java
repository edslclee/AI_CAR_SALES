package com.aicarsales.app.service;

import com.aicarsales.app.domain.User;
import java.util.Optional;

public interface UserService {
    User create(User user);
    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);
}

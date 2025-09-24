package com.aicarsales.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aicarsales.app.domain.User;
import com.aicarsales.app.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createShouldPersistUserViaRepository() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed");

        when(userRepository.save(user)).thenReturn(user);

        User saved = userService.create(user);

        assertThat(saved).isEqualTo(user);
        verify(userRepository).save(user);
    }

    @Test
    void findByEmailShouldDelegateToRepository() {
        User user = new User();
        user.setEmail("lookup@example.com");

        when(userRepository.findByEmail("lookup@example.com")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByEmail("lookup@example.com");

        assertThat(result).contains(user);
        verify(userRepository).findByEmail("lookup@example.com");
    }
}

package com.aicarsales.app.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.aicarsales.app.domain.AuthProvider;
import com.aicarsales.app.domain.User;
import com.aicarsales.app.repository.UserRepository;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@DataJpaTest
class CustomOAuth2UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;
    private CustomOAuth2UserService customOAuth2UserService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        OAuth2UserServiceStub delegate = new OAuth2UserServiceStub();
        customOAuth2UserService = new CustomOAuth2UserService(userRepository, passwordEncoder, delegate);
    }

    @Test
    void loadUserShouldCreateNewUser() {
        OAuth2UserRequest userRequest = buildUserRequest();
        customOAuth2UserService.loadUser(userRequest);

        User saved = userRepository.findByEmail("oauth.tester@example.com").orElseThrow();
        assertThat(saved.getAuthProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(saved.getProviderId()).isEqualTo("google-sub-123");
        assertThat(saved.getName()).isEqualTo("OAuth Tester");
        assertThat(saved.getPictureUrl()).isEqualTo("https://example.com/picture.png");
        assertThat(passwordEncoder.matches("google:google-sub-123", saved.getPasswordHash())).isTrue();
    }

    @Test
    void loadUserShouldUpdateExistingUser() {
        User existing = new User();
        existing.setEmail("oauth.tester@example.com");
        existing.setPasswordHash(passwordEncoder.encode("local"));
        existing.setAuthProvider(AuthProvider.LOCAL);
        existing.setRole("USER");
        userRepository.save(existing);

        OAuth2UserRequest userRequest = buildUserRequest();
        customOAuth2UserService.loadUser(userRequest);

        User saved = userRepository.findByEmail("oauth.tester@example.com").orElseThrow();
        assertThat(saved.getAuthProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(saved.getProviderId()).isEqualTo("google-sub-123");
        assertThat(saved.getName()).isEqualTo("OAuth Tester");
        assertThat(saved.getPictureUrl()).isEqualTo("https://example.com/picture.png");
    }

    private OAuth2UserRequest buildUserRequest() {
        ClientRegistration registration = ClientRegistration.withRegistrationId("google")
                .clientId("test-client")
                .clientSecret("test-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .authorizationUri("https://example.com/oauth2/auth")
                .tokenUri("https://example.com/oauth2/token")
                .userInfoUri("https://example.com/oauth2/userinfo")
                .userNameAttributeName("sub")
                .clientName("Google")
                .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(60),
                Set.of("openid", "profile", "email"));

        return new OAuth2UserRequest(registration, accessToken);
    }

    private static class OAuth2UserServiceStub implements org.springframework.security.oauth2.client.userinfo.OAuth2UserService<OAuth2UserRequest, OAuth2User> {
        @Override
        public OAuth2User loadUser(OAuth2UserRequest userRequest) {
            return new DefaultOAuth2User(
                    Set.of(new SimpleGrantedAuthority("ROLE_USER")),
                    Map.of(
                            "sub", "google-sub-123",
                            "email", "oauth.tester@example.com",
                            "name", "OAuth Tester",
                            "picture", "https://example.com/picture.png"
                    ),
                    "sub");
        }
    }
}

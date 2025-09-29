package com.aicarsales.app.security;

import com.aicarsales.app.domain.AuthProvider;
import com.aicarsales.app.domain.User;
import com.aicarsales.app.repository.UserRepository;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate;

    @Autowired
    public CustomOAuth2UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this(userRepository, passwordEncoder, new DefaultOAuth2UserService());
    }

    CustomOAuth2UserService(UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        AuthProvider provider = AuthProvider.fromRegistrationId(registrationId);

        String nameAttributeKey = userRequest
                .getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();
        if (nameAttributeKey == null || nameAttributeKey.isBlank()) {
            nameAttributeKey = "sub";
        }

        String providerId = getAttribute(attributes, nameAttributeKey);
        if (providerId == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token"),
                    "Missing provider identifier");
        }

        String email = getAttribute(attributes, "email");
        if (email == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token"),
                    "Email is required for OAuth2 login");
        }

        String displayName = getAttribute(attributes, "name");
        String picture = getAttribute(attributes, "picture");

        User user = userRepository.findByAuthProviderAndProviderId(provider, providerId)
                .or(() -> userRepository.findByEmail(email))
                .orElseGet(User::new);

        if (user.getId() == null) {
            user.setPasswordHash(passwordEncoder.encode(registrationId + ":" + providerId));
            user.setRole("USER");
        }

        user.setEmail(email);
        user.setName(displayName);
        user.setPictureUrl(picture);
        user.setAuthProvider(provider);
        user.setProviderId(providerId);
        userRepository.save(user);

        Collection<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
        return new DefaultOAuth2User(authorities, attributes, nameAttributeKey);
    }

    private String getAttribute(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        return value != null ? value.toString() : null;
    }
}

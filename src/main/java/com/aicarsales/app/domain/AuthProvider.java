package com.aicarsales.app.domain;

public enum AuthProvider {
    LOCAL,
    GOOGLE;

    public static AuthProvider fromRegistrationId(String registrationId) {
        if (registrationId == null) {
            throw new IllegalArgumentException("registrationId must not be null");
        }
        return switch (registrationId.toLowerCase()) {
            case "google" -> GOOGLE;
            default -> throw new IllegalArgumentException("Unsupported OAuth provider: " + registrationId);
        };
    }
}

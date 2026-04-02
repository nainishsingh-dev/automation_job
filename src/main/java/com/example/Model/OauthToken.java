package com.example.Model;

import java.time.Instant;

public class OauthToken {
        private String accessToken;
        private Instant expiresAt;

        public OauthToken(String accessToken, Instant expiresAt) {
            this.accessToken = accessToken;
            this.expiresAt = expiresAt;
        }

        public boolean isExpired() {
            return Instant.now().isAfter(expiresAt.minusSeconds(60)); // refresh 1 min early
        }

        public String getAccessToken() {
            return accessToken;
        }
    }


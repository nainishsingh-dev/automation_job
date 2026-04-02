package com.example.idcsService;

import com.example.Model.OauthToken;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.stream.Collectors;

@Component
public class IdcsTokenManager {
    @Value("${sso.domain}")
    private String ssoDomain;
    @Value("${sso.key}")
    private String ssoKey;
    private static String staticSsoDomain;
    private static String staticSsoKey;

    @PostConstruct
    public void init() {
        staticSsoDomain = ssoDomain;
        staticSsoKey=ssoKey;
    }

    public static OauthToken fetchNewToken() throws IOException {
        URL url = new URL(staticSsoDomain + "/oauth2/v1/token");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Authorization",
                "Basic "+staticSsoKey);

        String body = "grant_type=client_credentials&scope=urn:opc:idm:__myscopes__";
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String response = reader.lines().collect(Collectors.joining());
            JSONObject json = new JSONObject(response);
            String token = json.getString("access_token");
            int expiresIn = json.getInt("expires_in");
            Instant expiry = Instant.now().plusSeconds(expiresIn);
            return new OauthToken(token, expiry);
        }
    }
}
package tests;

import java.util.concurrent.ThreadLocalRandom;

public class TestConfig {
    static final String ENDPOINT_URL = "http://localhost:8080/endpoint";
    static final String API_KEY = "qazWSXedc";
    static final int MOCK_PORT = 8888;

    static final String VALID_TOKEN = "ABCDEF0123456789ABCDEF0123456789";

    private static final String TOKEN_ALPHABET = "ABCDEF0123456789";
    private static final int TOKEN_LENGTH = 32;

    static String newValidToken() {
        StringBuilder token = new StringBuilder(TOKEN_LENGTH);

        for (int i = 0; i < TOKEN_LENGTH; i++) {
            int randomIndex = ThreadLocalRandom.current().nextInt(TOKEN_ALPHABET.length());
            token.append(TOKEN_ALPHABET.charAt(randomIndex));
        }

        return token.toString();
    }

    private TestConfig() {
    }
}

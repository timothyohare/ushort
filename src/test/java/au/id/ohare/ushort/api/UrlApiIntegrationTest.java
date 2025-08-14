package au.id.ohare.ushort.api;

import au.id.ohare.ushort.entity.UrlEntity;
import au.id.ohare.ushort.repository.UrlRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UrlApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        
        // Configure RestTemplate to not follow redirects
        restTemplate.getRestTemplate().setRequestFactory(
            new org.springframework.http.client.SimpleClientHttpRequestFactory() {
                @Override
                protected void prepareConnection(java.net.HttpURLConnection connection, String httpMethod) throws java.io.IOException {
                    super.prepareConnection(connection, httpMethod);
                    connection.setInstanceFollowRedirects(false);
                }
            }
        );
    }

    @Test
    @DisplayName("Should create shortened URL via POST /shorten")
    void shouldCreateShortenedUrl() throws Exception {
        // Given
        String originalUrl = "https://www.example.com";
        Map<String, String> request = Map.of("url", originalUrl);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/shorten", 
                request, 
                String.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        // Parse JSON response
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        assertTrue(jsonResponse.has("shortenedUrl"), "Response should contain shortenedUrl");
        assertTrue(jsonResponse.has("originalUrl"), "Response should contain originalUrl");

        String shortenedUrl = jsonResponse.get("shortenedUrl").asText();
        String returnedOriginalUrl = jsonResponse.get("originalUrl").asText();

        assertEquals(originalUrl, returnedOriginalUrl);
        assertTrue(shortenedUrl.startsWith("http://localhost:" + port + "/"));

        // Extract shortened code and verify it's stored in database
        String shortenedCode = shortenedUrl.substring(shortenedUrl.lastIndexOf("/") + 1);
        Optional<UrlEntity> savedEntity = urlRepository.findByShortenedUrl(shortenedCode);
        assertTrue(savedEntity.isPresent(), "Shortened URL should be saved in database");
        assertEquals(originalUrl, savedEntity.get().getOriginalUrl());
        assertEquals(0, savedEntity.get().getAccessCount());
    }

    @Test
    @DisplayName("Should return same shortened URL for duplicate original URL")
    void shouldReturnSameShortenedUrlForDuplicate() throws Exception {
        // Given
        String originalUrl = "https://www.duplicate.com";
        Map<String, String> request = Map.of("url", originalUrl);

        // When - First request
        ResponseEntity<String> response1 = restTemplate.postForEntity(
                baseUrl + "/api/shorten", 
                request, 
                String.class
        );

        // When - Second request with same URL
        ResponseEntity<String> response2 = restTemplate.postForEntity(
                baseUrl + "/api/shorten", 
                request, 
                String.class
        );

        // Then
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());

        JsonNode json1 = objectMapper.readTree(response1.getBody());
        JsonNode json2 = objectMapper.readTree(response2.getBody());

        assertEquals(json1.get("shortenedUrl").asText(), json2.get("shortenedUrl").asText());
        assertEquals(1, urlRepository.count(), "Should not create duplicate entries");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.google.com",
            "http://example.org/path?param=value",
            "https://subdomain.example.co.uk/long/path/with/multiple/segments",
            "https://www.unicode-测试.com"
    })
    @DisplayName("Should handle various URL formats")
    void shouldHandleVariousUrlFormats(String originalUrl) throws Exception {
        // Given
        Map<String, String> request = Map.of("url", originalUrl);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/shorten", 
                request, 
                String.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        assertEquals(originalUrl, jsonResponse.get("originalUrl").asText());
        assertTrue(jsonResponse.get("shortenedUrl").asText().startsWith("http://localhost:" + port + "/"));
    }

    @Test
    @DisplayName("Should return 400 for invalid URL input")
    void shouldReturn400ForInvalidUrl() {
        // Given - Invalid URL
        Map<String, String> request = Map.of("url", "not-a-valid-url");

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/shorten", 
                request, 
                String.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 400 for empty URL input")
    void shouldReturn400ForEmptyUrl() {
        // Given - Empty URL
        Map<String, String> request = Map.of("url", "");

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/shorten", 
                request, 
                String.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 400 for missing URL parameter")
    void shouldReturn400ForMissingUrl() {
        // Given - Missing URL parameter
        Map<String, String> request = Map.of("notUrl", "https://www.example.com");

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/shorten", 
                request, 
                String.class
        );

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Should redirect to original URL via GET /{shortenedUrl}")
    void shouldRedirectToOriginalUrl() {
        // Given - Create a URL entity in database
        String originalUrl = "https://www.redirect-test.com";
        String shortenedCode = "test123456";
        UrlEntity urlEntity = UrlEntity.builder()
                .originalUrl(originalUrl)
                .shortenedUrl(shortenedCode)
                .accessCount(0)
                .lastAccessed(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
        urlRepository.save(urlEntity);

        // When - Access the shortened URL
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/" + shortenedCode, 
                String.class
        );

        // Then
        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals(originalUrl, response.getHeaders().getLocation().toString());

        // Verify access count was incremented
        Optional<UrlEntity> updatedEntity = urlRepository.findByShortenedUrl(shortenedCode);
        assertTrue(updatedEntity.isPresent());
        assertEquals(1, updatedEntity.get().getAccessCount());
    }

    @Test
    @DisplayName("Should return 404 for non-existent shortened URL")
    void shouldReturn404ForNonExistentUrl() {
        // When - Access non-existent shortened URL
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/nonexistent", 
                String.class
        );

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 410 for expired shortened URL")
    void shouldReturn410ForExpiredUrl() {
        // Given - Create an expired URL entity (older than 90 days)
        String originalUrl = "https://www.expired-test.com";
        String shortenedCode = "expired123";
        UrlEntity expiredEntity = UrlEntity.builder()
                .originalUrl(originalUrl)
                .shortenedUrl(shortenedCode)
                .accessCount(5)
                .lastAccessed(LocalDateTime.now().minusDays(91)) // Expired
                .createdAt(LocalDateTime.now().minusDays(100))
                .build();
        urlRepository.save(expiredEntity);

        // When - Access the expired shortened URL
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/" + shortenedCode, 
                String.class
        );

        // Then
        assertEquals(HttpStatus.GONE, response.getStatusCode());

        // Verify the expired URL was deleted from database
        Optional<UrlEntity> deletedEntity = urlRepository.findByShortenedUrl(shortenedCode);
        assertFalse(deletedEntity.isPresent(), "Expired URL should be deleted from database");
    }

    @Test
    @DisplayName("Should update last accessed time on successful redirect")
    void shouldUpdateLastAccessedTime() throws InterruptedException {
        // Given - Create a URL entity
        String originalUrl = "https://www.timestamp-test.com";
        String shortenedCode = "time123456";
        LocalDateTime initialTime = LocalDateTime.now().minusHours(1);
        UrlEntity urlEntity = UrlEntity.builder()
                .originalUrl(originalUrl)
                .shortenedUrl(shortenedCode)
                .accessCount(0)
                .lastAccessed(initialTime)
                .createdAt(LocalDateTime.now())
                .build();
        urlRepository.save(urlEntity);

        // Wait a moment to ensure time difference
        Thread.sleep(100);

        // When - Access the shortened URL
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/" + shortenedCode, 
                String.class
        );

        // Then
        assertEquals(HttpStatus.FOUND, response.getStatusCode());

        // Verify last accessed time was updated
        Optional<UrlEntity> updatedEntity = urlRepository.findByShortenedUrl(shortenedCode);
        assertTrue(updatedEntity.isPresent());
        assertTrue(updatedEntity.get().getLastAccessed().isAfter(initialTime), 
                "Last accessed time should be updated");
    }

    @Test
    @DisplayName("Should handle concurrent access to same shortened URL")
    void shouldHandleConcurrentAccess() throws InterruptedException {
        // Given - Create a URL entity
        String originalUrl = "https://www.concurrent-test.com";
        String shortenedCode = "conc123456";
        UrlEntity urlEntity = UrlEntity.builder()
                .originalUrl(originalUrl)
                .shortenedUrl(shortenedCode)
                .accessCount(0)
                .lastAccessed(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
        urlRepository.save(urlEntity);

        // When - Make multiple concurrent requests
        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            threads[i] = new Thread(() -> {
                ResponseEntity<String> response = restTemplate.getForEntity(
                        baseUrl + "/api/" + shortenedCode, 
                        String.class
                );
                assertEquals(HttpStatus.FOUND, response.getStatusCode());
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Then - Verify access count was incremented correctly
        Optional<UrlEntity> updatedEntity = urlRepository.findByShortenedUrl(shortenedCode);
        assertTrue(updatedEntity.isPresent());
        assertEquals(5, updatedEntity.get().getAccessCount());
    }

    @Test
    @DisplayName("Should set proper Content-Type headers")
    void shouldSetProperContentTypeHeaders() throws Exception {
        // Given
        String originalUrl = "https://www.headers-test.com";
        Map<String, String> request = Map.of("url", originalUrl);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/shorten", 
                request, 
                String.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().getContentType().includes(MediaType.APPLICATION_JSON),
                "Content-Type should be application/json");
    }
}
package au.id.ohare.ushort.admin;

import au.id.ohare.ushort.entity.UrlEntity;
import au.id.ohare.ushort.repository.UrlRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "admin.username=testadmin",
        "admin.password=testpass123",
        "logging.level.au.id.ohare.ushort=DEBUG"
})
class AdminAnalyticsIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;
    private String adminCredentials;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        
        // Clear database before each test
        urlRepository.deleteAll();
        
        // Create Basic Auth header with test credentials
        String auth = "testadmin:testpass123";
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        adminCredentials = "Basic " + new String(encodedAuth);
    }

    @Test
    @DisplayName("Should return 401 when accessing admin analytics without authentication")
    void shouldReturn401WithoutAuth() {
        // When - Access admin analytics without authentication
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/admin/analytics", 
                String.class
        );

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return admin analytics page with valid authentication")
    void shouldReturnAnalyticsPageWithValidAuth() {
        // Given - Valid admin credentials
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", adminCredentials);
        HttpEntity<String> request = new HttpEntity<>(headers);

        // When - Access admin analytics with valid credentials
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/admin/analytics",
                HttpMethod.GET,
                request,
                String.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Admin Analytics"));
        assertTrue(response.getBody().contains("Total URLs"));
    }

    @Test
    @DisplayName("Should display correct URL count and statistics")
    void shouldDisplayCorrectUrlStatistics() {
        // Given - Some URLs in database
        UrlEntity url1 = createTestUrl("https://example.com", "abc123", 5);
        UrlEntity url2 = createTestUrl("https://google.com", "def456", 10);
        
        urlRepository.save(url1);
        urlRepository.save(url2);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", adminCredentials);
        HttpEntity<String> request = new HttpEntity<>(headers);

        // When - Access admin analytics
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/admin/analytics",
                HttpMethod.GET,
                request,
                String.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);
        
        // Check individual URL statistics
        assertTrue(body.contains("abc123"));
        assertTrue(body.contains("def456"));
        assertTrue(body.contains("https://example.com"));
        assertTrue(body.contains("https://google.com"));
    }

    private UrlEntity createTestUrl(String originalUrl, String shortenedUrl, int accessCount) {
        return UrlEntity.builder()
                .originalUrl(originalUrl)
                .shortenedUrl(shortenedUrl)
                .accessCount(accessCount)
                .createdAt(LocalDateTime.now().minusDays(1))
                .lastAccessed(LocalDateTime.now().minusHours(1))
                .build();
    }
}
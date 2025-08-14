package au.id.ohare.ushort.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UrlShortenerServiceTest {

    private UrlShortenerService urlShortenerService;

    @BeforeEach
    void setUp() {
        urlShortenerService = new UrlShortenerService();
    }

    @Test
    @DisplayName("Should generate shortened URL code from original URL")
    void shouldGenerateShortenedCode() {
        String originalUrl = "https://www.example.com";
        
        String shortenedCode = urlShortenerService.generateShortenedCode(originalUrl);
        
        assertNotNull(shortenedCode, "Shortened code should not be null");
        assertFalse(shortenedCode.isEmpty(), "Shortened code should not be empty");
    }

    @Test
    @DisplayName("Should generate consistent shortened codes for same URL")
    void shouldGenerateConsistentCodes() {
        String originalUrl = "https://www.example.com";
        
        String code1 = urlShortenerService.generateShortenedCode(originalUrl);
        String code2 = urlShortenerService.generateShortenedCode(originalUrl);
        
        assertEquals(code1, code2, "Same URL should generate same shortened code");
    }

    @Test
    @DisplayName("Should generate different codes for different URLs")
    void shouldGenerateDifferentCodesForDifferentUrls() {
        String url1 = "https://www.example.com";
        String url2 = "https://www.google.com";
        
        String code1 = urlShortenerService.generateShortenedCode(url1);
        String code2 = urlShortenerService.generateShortenedCode(url2);
        
        assertNotEquals(code1, code2, "Different URLs should generate different codes");
    }

    @Test
    @DisplayName("Should generate code with length between 6-8 characters")
    void shouldGenerateCodeWithCorrectLength() {
        String originalUrl = "https://www.example.com";
        
        String shortenedCode = urlShortenerService.generateShortenedCode(originalUrl);
        
        assertTrue(shortenedCode.length() >= 6 && shortenedCode.length() <= 8,
                "Shortened code length should be between 6-8 characters, but was: " + shortenedCode.length());
    }

    @Test
    @DisplayName("Should generate code using only Base62 characters (0-9, a-z, A-Z)")
    void shouldUseBase62Characters() {
        String originalUrl = "https://www.example.com";
        
        String shortenedCode = urlShortenerService.generateShortenedCode(originalUrl);
        
        assertTrue(shortenedCode.matches("^[0-9a-zA-Z]+$"),
                "Shortened code should only contain Base62 characters: " + shortenedCode);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.example.com",
            "http://google.com",
            "https://very-long-domain-name-that-should-still-work.com/path/to/resource?param=value",
            "https://unicode-测试.com",
            "https://example.com/path with spaces"
    })
    @DisplayName("Should handle various URL formats")
    void shouldHandleVariousUrlFormats(String originalUrl) {
        String shortenedCode = urlShortenerService.generateShortenedCode(originalUrl);
        
        assertNotNull(shortenedCode);
        assertFalse(shortenedCode.isEmpty());
        assertTrue(shortenedCode.matches("^[0-9a-zA-Z]+$"));
        assertTrue(shortenedCode.length() >= 6 && shortenedCode.length() <= 8);
    }

    @Test
    @DisplayName("Should not contain rude words")
    void shouldNotContainRudeWords() {
        // Test multiple URLs to increase chance of hitting rude words
        Set<String> testUrls = Set.of(
                "https://www.example1.com",
                "https://www.example2.com", 
                "https://www.example3.com",
                "https://www.example4.com",
                "https://www.example5.com",
                "https://test1.com",
                "https://test2.com",
                "https://test3.com",
                "https://sample1.org",
                "https://sample2.org"
        );
        
        for (String url : testUrls) {
            String shortenedCode = urlShortenerService.generateShortenedCode(url);
            
            assertFalse(urlShortenerService.containsRudeWords(shortenedCode),
                    "Shortened code should not contain rude words: " + shortenedCode);
        }
    }

    @Test
    @DisplayName("Should detect rude words correctly")
    void shouldDetectRudeWordsCorrectly() {
        // Test known rude words to ensure filter works
        assertTrue(urlShortenerService.containsRudeWords("damn"), "Should detect 'damn' as rude word");
        assertTrue(urlShortenerService.containsRudeWords("DAMN"), "Should detect 'DAMN' as rude word (case insensitive)");
        assertTrue(urlShortenerService.containsRudeWords("shit"), "Should detect 'shit' as rude word");
        assertTrue(urlShortenerService.containsRudeWords("hell"), "Should detect 'hell' as rude word");
        assertTrue(urlShortenerService.containsRudeWords("hellX"), "Should detect 'hellX' as rude word (starts with hell)");
        assertTrue(urlShortenerService.containsRudeWords("Xhell"), "Should detect 'Xhell' as rude word (ends with hell)");
        
        // Test clean words that don't start or end with rude words
        assertFalse(urlShortenerService.containsRudeWords("test123"), "Should not detect 'test123' as rude word");
        assertFalse(urlShortenerService.containsRudeWords("abc123"), "Should not detect 'abc123' as rude word");
        assertFalse(urlShortenerService.containsRudeWords("xyz789"), "Should not detect 'xyz789' as rude word");
    }

    @Test
    @DisplayName("Should handle null or empty URL input gracefully")
    void shouldHandleInvalidInput() {
        assertThrows(IllegalArgumentException.class, 
                () -> urlShortenerService.generateShortenedCode(null),
                "Should throw exception for null URL");
        
        assertThrows(IllegalArgumentException.class,
                () -> urlShortenerService.generateShortenedCode(""),
                "Should throw exception for empty URL");
        
        assertThrows(IllegalArgumentException.class,
                () -> urlShortenerService.generateShortenedCode("   "),
                "Should throw exception for whitespace-only URL");
    }
}
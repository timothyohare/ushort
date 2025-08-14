package au.id.ohare.ushort.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

@Slf4j
@Service
public class UrlShortenerService {

    private static final String BASE62_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int HASH_BYTES_LENGTH = 6;
    private static final int MIN_CODE_LENGTH = 6;
    private static final int MAX_CODE_LENGTH = 8;
    private static final int MAX_RUDE_WORD_ATTEMPTS = 100;
    
    // Basic set of rude words to filter out
    private static final Set<String> RUDE_WORDS = Set.of(
            "damn", "shit", "hell", "fuck", "ass", "piss", "crap", "bitch",
            "bastard", "turd", "puke", "fart", "butt", "sex", "porn", "xxx"
    );

    public String generateShortenedCode(String originalUrl) {
        if (originalUrl == null || originalUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }

        String trimmedUrl = originalUrl.trim();
        log.debug("Generating shortened code for URL: {}", trimmedUrl);
        
        String hashedCode;
        int attempt = 0;
        
        do {
            // Generate SHA-256 hash with attempt counter to avoid rude words
            String input = trimmedUrl + (attempt > 0 ? "_" + attempt : "");
            hashedCode = generateHashedCode(input);
            attempt++;
            
            if (containsRudeWords(hashedCode)) {
                log.debug("Generated code '{}' contains rude words, attempting again (attempt {})", hashedCode, attempt);
            }
        } while (containsRudeWords(hashedCode) && attempt < MAX_RUDE_WORD_ATTEMPTS);
        
        if (containsRudeWords(hashedCode)) {
            log.error("Unable to generate clean shortened code for URL '{}' after {} attempts", trimmedUrl, MAX_RUDE_WORD_ATTEMPTS);
            throw new RuntimeException("Unable to generate clean shortened code after multiple attempts");
        }
        
        log.debug("Successfully generated shortened code '{}' for URL '{}' after {} attempts", hashedCode, trimmedUrl, attempt);
        return hashedCode;
    }

    private String generateHashedCode(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            // Take first bytes (will give us 6-8 character Base62 string)
            byte[] truncatedHash = new byte[HASH_BYTES_LENGTH];
            System.arraycopy(hashBytes, 0, truncatedHash, 0, HASH_BYTES_LENGTH);
            
            return encodeBase62(truncatedHash);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private String encodeBase62(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        
        // Convert bytes to a large integer and encode using Base62
        java.math.BigInteger bigInteger = new java.math.BigInteger(1, bytes);
        java.math.BigInteger base = java.math.BigInteger.valueOf(62);
        
        while (bigInteger.compareTo(java.math.BigInteger.ZERO) > 0) {
            java.math.BigInteger remainder = bigInteger.remainder(base);
            result.insert(0, BASE62_ALPHABET.charAt(remainder.intValue()));
            bigInteger = bigInteger.divide(base);
        }
        
        // Ensure minimum length by padding with first character of alphabet
        while (result.length() < MIN_CODE_LENGTH) {
            result.insert(0, BASE62_ALPHABET.charAt(0));
        }
        
        // Limit to maximum length
        if (result.length() > MAX_CODE_LENGTH) {
            result = new StringBuilder(result.substring(0, MAX_CODE_LENGTH));
        }
        
        return result.toString();
    }

    public boolean containsRudeWords(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        
        String lowerCode = code.toLowerCase();
        
        for (String rudeWord : RUDE_WORDS) {
            // Check if the rude word appears as a whole word (not just substring)
            if (lowerCode.equals(rudeWord.toLowerCase()) || 
                lowerCode.startsWith(rudeWord.toLowerCase()) ||
                lowerCode.endsWith(rudeWord.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }
}
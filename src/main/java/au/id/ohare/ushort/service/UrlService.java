package au.id.ohare.ushort.service;

import au.id.ohare.ushort.entity.UrlEntity;
import au.id.ohare.ushort.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {

    private static final String EXPIRED_URL_MARKER = "EXPIRED";

    private final UrlRepository urlRepository;
    private final UrlShortenerService urlShortenerService;
    
    @Value("${app.base-url:http://localhost}")
    private String baseUrl;

    @Value("${app.ttl-days:90}")
    private int ttlDays;

    @Transactional
    public UrlEntity createShortenedUrl(String originalUrl) {
        log.info("Creating shortened URL for: {}", originalUrl);

        // Check if URL already exists
        String shortenedCode = urlShortenerService.generateShortenedCode(originalUrl);
        Optional<UrlEntity> existingUrl = urlRepository.findByShortenedUrl(shortenedCode);
        
        if (existingUrl.isPresent()) {
            log.debug("URL already exists: {}", shortenedCode);
            return existingUrl.get();
        }

        // Create new URL entity
        UrlEntity urlEntity = UrlEntity.builder()
                .originalUrl(originalUrl)
                .shortenedUrl(shortenedCode)
                .accessCount(0)
                .lastAccessed(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        UrlEntity savedEntity = urlRepository.save(urlEntity);
        log.info("URL created: shortCode={}, originalUrl={}", shortenedCode, originalUrl);
        
        return savedEntity;
    }

    @Transactional
    public Optional<String> getOriginalUrl(String shortenedCode, String clientIp) {
        log.debug("Looking up shortened URL: {}", shortenedCode);
        
        Optional<UrlEntity> urlEntityOpt = urlRepository.findByShortenedUrl(shortenedCode);
        
        if (urlEntityOpt.isEmpty()) {
            log.warn("Shortened URL not found: {}", shortenedCode);
            return Optional.empty();
        }

        UrlEntity urlEntity = urlEntityOpt.get();

        // Check if URL has expired
        LocalDateTime expiryDate = urlEntity.getLastAccessed().plusDays(ttlDays);
        if (LocalDateTime.now().isAfter(expiryDate)) {
            log.warn("URL expired: shortCode={}, lastAccessed={}", shortenedCode, urlEntity.getLastAccessed());
            
            // Delete expired URL
            urlRepository.delete(urlEntity);
            log.info("Deleted expired URL: {}", shortenedCode);
            
            return Optional.of(EXPIRED_URL_MARKER);
        }

        // Update access information atomically
        LocalDateTime now = LocalDateTime.now();
        int updatedRows = urlRepository.incrementAccessCount(shortenedCode, now);
        
        if (updatedRows > 0) {
            // Reload the entity to get the updated access count for logging
            Optional<UrlEntity> updatedEntity = urlRepository.findByShortenedUrl(shortenedCode);
            int newAccessCount = updatedEntity.map(UrlEntity::getAccessCount).orElse(-1);
            
            log.info("URL accessed: shortCode={}, clientIp={}, newAccessCount={}", 
                    shortenedCode, clientIp, newAccessCount);
        } else {
            log.warn("Failed to update access count for: {}", shortenedCode);
        }
        
        return Optional.of(urlEntity.getOriginalUrl());
    }

    public String buildFullShortenedUrl(String shortenedCode, String serverUrl) {
        // Use server URL from request context if available
        if (serverUrl != null && !serverUrl.isEmpty()) {
            return serverUrl + "/" + shortenedCode;
        }
        return baseUrl + "/" + shortenedCode;
    }

    public boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        try {
            String trimmedUrl = url.trim();
            return trimmedUrl.startsWith("http://") || trimmedUrl.startsWith("https://");
        } catch (Exception e) {
            log.debug("Invalid URL format: {}", url, e);
            return false;
        }
    }

    public long getTotalUrlCount() {
        return urlRepository.count();
    }

    @Transactional
    public void cleanupExpiredUrls() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(ttlDays);
        var expiredUrls = urlRepository.findByLastAccessedBefore(cutoffDate);
        
        if (!expiredUrls.isEmpty()) {
            log.info("Cleaning up {} expired URLs", expiredUrls.size());
            urlRepository.deleteAll(expiredUrls);
            log.info("Cleanup complete: {} URLs deleted", expiredUrls.size());
        } else {
            log.debug("No expired URLs found");
        }
    }
}
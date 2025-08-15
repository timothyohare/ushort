package au.id.ohare.ushort.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LoggingService {

    public void logApplicationStartup(String version, String profile) {
        log.info("Application started: version={}, profile={}", version, profile);
    }

    public void logUrlCreation(String shortCode, String originalUrl, long durationMs) {
        log.info("URL created: shortCode={}, originalUrl={}, duration={}ms", 
                shortCode, originalUrl, durationMs);
    }

    public void logUrlAccess(String shortCode, String clientIp, String userAgent) {
        log.info("URL accessed: shortCode={}, clientIp={}, userAgent={}", 
                shortCode, clientIp, userAgent);
    }

    public void logAdminAccess(String username, String sessionId) {
        log.info("Admin accessed analytics: user={}, sessionId={}", username, sessionId);
    }

    public void logInvalidUrl(String url, String reason) {
        log.warn("Invalid URL provided: url={}, reason={}", url, reason);
    }

    public void logExpiredUrlAccess(String shortCode, String lastAccessed, int ttlDays) {
        log.warn("Expired URL accessed: shortCode={}, lastAccessed={}, ttlDays={}", 
                shortCode, lastAccessed, ttlDays);
    }

    public void logSystemError(String originalUrl, String errorMessage, Exception exception) {
        log.error("Failed to shorten URL: originalUrl={}, error={}", 
                originalUrl, errorMessage, exception);
    }

    public void logDatabaseOperation(String table, String operation, long durationMs) {
        log.debug("Database query executed: table={}, operation={}, duration={}ms", 
                table, operation, durationMs);
    }

    public void logAuthenticationFailure(String username, String ip, String reason) {
        log.warn("Authentication failed: username={}, ip={}, reason={}", 
                username, ip, reason);
    }

    public void logPerformanceMetrics(String endpoint, long responseTimeMs, int status) {
        log.info("Performance metrics: endpoint={}, responseTime={}ms, status={}", 
                endpoint, responseTimeMs, status);
    }
}
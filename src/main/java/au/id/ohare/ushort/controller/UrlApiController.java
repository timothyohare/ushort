package au.id.ohare.ushort.controller;

import au.id.ohare.ushort.dto.ShortenUrlRequest;
import au.id.ohare.ushort.dto.ShortenUrlResponse;
import au.id.ohare.ushort.entity.UrlEntity;
import au.id.ohare.ushort.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class UrlApiController {

    private static final String EXPIRED_URL_MARKER = "EXPIRED";
    private static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";
    private static final String X_REAL_IP_HEADER = "X-Real-IP";

    private final UrlService urlService;

    @PostMapping("/shorten")
    public ResponseEntity<ShortenUrlResponse> shortenUrl(
            @Valid @RequestBody ShortenUrlRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {
        
        String clientIp = getClientIp(httpRequest);
        String originalUrl = request != null ? request.getUrl() : null;
        
        log.debug("Received shorten request: url={}, clientIp={}", originalUrl, clientIp);

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors in request: {}, clientIp={}", bindingResult.getFieldErrors(), clientIp);
            return ResponseEntity.badRequest().build();
        }

        // Additional URL validation
        if (!urlService.isValidUrl(originalUrl)) {
            log.warn("Invalid URL format received: url={}, clientIp={}", originalUrl, clientIp);
            return ResponseEntity.badRequest().build();
        }

        try {
            UrlEntity urlEntity = urlService.createShortenedUrl(originalUrl);
            
            // Build server URL from request
            String serverUrl = buildServerUrl(httpRequest);
            String fullShortenedUrl = urlService.buildFullShortenedUrl(urlEntity.getShortenedUrl(), serverUrl);

            ShortenUrlResponse response = ShortenUrlResponse.builder()
                    .originalUrl(originalUrl)
                    .shortenedUrl(fullShortenedUrl)
                    .build();

            log.info("URL shortened successfully: originalUrl={}, shortenedUrl={}, clientIp={}", 
                    originalUrl, fullShortenedUrl, clientIp);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to shorten URL: originalUrl={}, clientIp={}, error={}", 
                    originalUrl, clientIp, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{shortenedCode}")
    public ResponseEntity<Void> redirectToOriginalUrl(
            @PathVariable String shortenedCode,
            HttpServletRequest request) {
        
        String clientIp = getClientIp(request);
        log.debug("Received redirect request: shortenedCode={}, clientIp={}", shortenedCode, clientIp);

        Optional<String> originalUrlOpt = urlService.getOriginalUrl(shortenedCode, clientIp);

        if (originalUrlOpt.isEmpty()) {
            log.warn("Shortened URL not found: shortenedCode={}, clientIp={}", shortenedCode, clientIp);
            return ResponseEntity.notFound().build();
        }

        String originalUrl = originalUrlOpt.get();

        if (EXPIRED_URL_MARKER.equals(originalUrl)) {
            log.warn("Shortened URL expired: shortenedCode={}, clientIp={}", shortenedCode, clientIp);
            return ResponseEntity.status(HttpStatus.GONE).build();
        }

        try {
            URI redirectUri = URI.create(originalUrl);
            log.info("Redirecting to original URL: shortenedCode={}, originalUrl={}, clientIp={}", 
                    shortenedCode, originalUrl, clientIp);
            
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(redirectUri)
                    .build();

        } catch (Exception e) {
            log.error("Failed to redirect: shortenedCode={}, originalUrl={}, clientIp={}, error={}", 
                    shortenedCode, originalUrl, clientIp, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader(X_FORWARDED_FOR_HEADER);
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader(X_REAL_IP_HEADER);
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private String buildServerUrl(HttpServletRequest request) {
        StringBuilder serverUrl = new StringBuilder();
        serverUrl.append(request.getScheme()).append("://");
        serverUrl.append(request.getServerName());
        
        int port = request.getServerPort();
        if ((request.getScheme().equals("http") && port != 80) ||
            (request.getScheme().equals("https") && port != 443)) {
            serverUrl.append(":").append(port);
        }
        
        return serverUrl.toString();
    }
}
package au.id.ohare.ushort.web;

import au.id.ohare.ushort.dto.ShortenUrlRequest;
import au.id.ohare.ushort.entity.UrlEntity;
import au.id.ohare.ushort.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UrlWebController {

    private static final String EXPIRED_URL_MARKER = "EXPIRED";
    private static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";
    private static final String X_REAL_IP_HEADER = "X-Real-IP";

    private final UrlService urlService;

    @GetMapping("/")
    public String showHomePage(Model model) {
        log.debug("Displaying home page");
        model.addAttribute("shortenUrlRequest", new ShortenUrlRequest());
        return "index";
    }

    @PostMapping("/")
    public String createShortenedUrl(
            @Valid ShortenUrlRequest shortenUrlRequest,
            BindingResult bindingResult,
            Model model,
            HttpServletRequest request) {
        
        String clientIp = getClientIp(request);
        String originalUrl = shortenUrlRequest != null ? shortenUrlRequest.getUrl() : null;
        
        log.debug("Processing URL shortening request: url={}, clientIp={}", originalUrl, clientIp);

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors in form submission: {}, clientIp={}", bindingResult.getFieldErrors(), clientIp);
            return "index";
        }

        if (!urlService.isValidUrl(originalUrl)) {
            log.warn("Invalid URL format submitted: url={}, clientIp={}", originalUrl, clientIp);
            model.addAttribute("error", "Please enter a valid URL that starts with http:// or https://");
            return "index";
        }

        try {
            UrlEntity urlEntity = urlService.createShortenedUrl(originalUrl);
            
            String serverUrl = buildServerUrl(request);
            String fullShortenedUrl = urlService.buildFullShortenedUrl(urlEntity.getShortenedUrl(), serverUrl);

            model.addAttribute("originalUrl", originalUrl);
            model.addAttribute("shortenedUrl", fullShortenedUrl);

            log.info("URL shortened successfully via web interface: originalUrl={}, shortenedUrl={}, clientIp={}", 
                    originalUrl, fullShortenedUrl, clientIp);

            return "success";

        } catch (Exception e) {
            log.error("Failed to shorten URL via web interface: originalUrl={}, clientIp={}, error={}", 
                    originalUrl, clientIp, e.getMessage(), e);
            model.addAttribute("errorMessage", "An error occurred while shortening the URL. Please try again.");
            return "error";
        }
    }

    @GetMapping("/{shortenedCode}")
    public String redirectToOriginalUrl(
            @PathVariable String shortenedCode,
            Model model,
            HttpServletRequest request) {
        
        String clientIp = getClientIp(request);
        log.debug("Processing redirection request: shortenedCode={}, clientIp={}", shortenedCode, clientIp);

        Optional<String> originalUrlOpt = urlService.getOriginalUrl(shortenedCode, clientIp);

        if (originalUrlOpt.isEmpty()) {
            log.warn("Shortened URL not found via web interface: shortenedCode={}, clientIp={}", shortenedCode, clientIp);
            model.addAttribute("errorMessage", "The shortened URL you requested was not found.");
            model.addAttribute("isNotFound", true);
            return "error";
        }

        String originalUrl = originalUrlOpt.get();

        if (EXPIRED_URL_MARKER.equals(originalUrl)) {
            log.warn("Shortened URL expired via web interface: shortenedCode={}, clientIp={}", shortenedCode, clientIp);
            model.addAttribute("errorMessage", "The shortened URL you requested has expired.");
            model.addAttribute("isExpired", true);
            return "error";
        }

        log.info("Redirecting via web interface: shortenedCode={}, originalUrl={}, clientIp={}", 
                shortenedCode, originalUrl, clientIp);
        
        return "redirect:" + originalUrl;
    }

    @GetMapping("/error")
    public String showErrorPage(Model model, HttpServletRequest request) {
        Object statusCode = request.getAttribute("javax.servlet.error.status_code");
        Object errorMessage = request.getAttribute("javax.servlet.error.message");
        
        log.debug("Displaying error page: statusCode={}, message={}", statusCode, errorMessage);
        
        if (!model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", "An error occurred while processing your request.");
        }
        
        return "error";
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
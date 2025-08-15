package au.id.ohare.ushort.web;

import au.id.ohare.ushort.entity.UrlEntity;
import au.id.ohare.ushort.service.UrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = UrlWebController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
class UrlWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlService urlService;

    @Test
    void homePage_ShouldDisplayUrlCreationForm() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("shortenUrlRequest"))
                .andExpect(content().string(containsString("URL Shortener")))
                .andExpect(content().string(containsString("Enter a URL to shorten")))
                .andExpect(content().string(containsString("form")))
                .andExpect(content().string(containsString("input")))
                .andExpect(content().string(containsString("button")))
                .andExpect(content().string(containsString("bootstrap")));
    }

    @Test
    void createShortenedUrl_WithValidUrl_ShouldDisplaySuccessPage() throws Exception {
        UrlEntity mockEntity = UrlEntity.builder()
                .id(1L)
                .originalUrl("https://example.com")
                .shortenedUrl("abc123")
                .accessCount(0)
                .createdAt(LocalDateTime.now())
                .lastAccessed(LocalDateTime.now())
                .build();

        when(urlService.isValidUrl("https://example.com")).thenReturn(true);
        when(urlService.createShortenedUrl("https://example.com")).thenReturn(mockEntity);
        when(urlService.buildFullShortenedUrl(anyString(), anyString())).thenReturn("http://localhost/abc123");

        mockMvc.perform(post("/")
                        .param("url", "https://example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("success"))
                .andExpect(model().attribute("originalUrl", "https://example.com"))
                .andExpect(model().attribute("shortenedUrl", "http://localhost/abc123"))
                .andExpect(content().string(containsString("URL Shortened Successfully")))
                .andExpect(content().string(containsString("https://example.com")))
                .andExpect(content().string(containsString("http://localhost/abc123")))
                .andExpect(content().string(containsString("Copy")))
                .andExpect(content().string(containsString("Create Another")))
                .andExpect(content().string(containsString("bootstrap")));
    }

    @Test
    void createShortenedUrl_WithEmptyUrl_ShouldDisplayValidationError() throws Exception {
        mockMvc.perform(post("/")
                        .param("url", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("shortenUrlRequest", "url"))
                .andExpect(content().string(containsString("URL is required")))
                .andExpect(content().string(containsString("bootstrap")));
    }

    @Test
    void createShortenedUrl_WithInvalidUrl_ShouldDisplayValidationError() throws Exception {
        when(urlService.isValidUrl("invalid-url")).thenReturn(false);

        mockMvc.perform(post("/")
                        .param("url", "invalid-url"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Please enter a valid URL that starts with http:// or https://"))
                .andExpect(content().string(containsString("Please enter a valid URL")))
                .andExpect(content().string(containsString("bootstrap")));
    }

    @Test
    void createShortenedUrl_WithServiceException_ShouldDisplayErrorPage() throws Exception {
        when(urlService.isValidUrl("https://example.com")).thenReturn(true);
        when(urlService.createShortenedUrl("https://example.com")).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/")
                        .param("url", "https://example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(content().string(containsString("An error occurred")))
                .andExpect(content().string(containsString("Try Again")))
                .andExpect(content().string(containsString("bootstrap")));
    }

    @Test
    void redirectToOriginalUrl_WithValidCode_ShouldRedirect() throws Exception {
        when(urlService.getOriginalUrl(eq("abc123"), anyString())).thenReturn(Optional.of("https://example.com"));

        mockMvc.perform(get("/abc123"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("https://example.com"));
    }

    @Test
    void redirectToOriginalUrl_WithInvalidCode_ShouldDisplayErrorPage() throws Exception {
        when(urlService.getOriginalUrl(eq("invalid"), anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/invalid"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorMessage", "The shortened URL you requested was not found."))
                .andExpect(content().string(containsString("URL Not Found")))
                .andExpect(content().string(containsString("The shortened URL you requested was not found")))
                .andExpect(content().string(containsString("Go to Home Page")))
                .andExpect(content().string(containsString("bootstrap")));
    }

    @Test
    void redirectToOriginalUrl_WithExpiredCode_ShouldDisplayErrorPage() throws Exception {
        when(urlService.getOriginalUrl(eq("expired"), anyString())).thenReturn(Optional.of("EXPIRED"));

        mockMvc.perform(get("/expired"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorMessage", "The shortened URL you requested has expired."))
                .andExpect(content().string(containsString("URL Expired")))
                .andExpect(content().string(containsString("The shortened URL you requested has expired")))
                .andExpect(content().string(containsString("Go to Home Page")))
                .andExpect(content().string(containsString("bootstrap")));
    }

    @Test
    void errorPage_ShouldDisplayGenericErrorWithHomeLink() throws Exception {
        mockMvc.perform(get("/error")
                        .requestAttr("javax.servlet.error.status_code", 500)
                        .requestAttr("javax.servlet.error.message", "Internal Server Error"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(content().string(containsString("An error occurred")))
                .andExpect(content().string(containsString("Go to Home Page")))
                .andExpect(content().string(containsString("bootstrap")));
    }

    @Test
    void responsiveDesign_ShouldIncludeBootstrapAndMeta() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("viewport")))
                .andExpect(content().string(containsString("bootstrap")))
                .andExpect(content().string(containsString("container")))
                .andExpect(content().string(containsString("responsive")));
    }

    @Test
    void copyToClipboard_ShouldIncludeJavaScriptFunction() throws Exception {
        UrlEntity mockEntity = UrlEntity.builder()
                .id(1L)
                .originalUrl("https://example.com")
                .shortenedUrl("abc123")
                .accessCount(0)
                .createdAt(LocalDateTime.now())
                .lastAccessed(LocalDateTime.now())
                .build();

        when(urlService.isValidUrl("https://example.com")).thenReturn(true);
        when(urlService.createShortenedUrl("https://example.com")).thenReturn(mockEntity);
        when(urlService.buildFullShortenedUrl(anyString(), anyString())).thenReturn("http://localhost/abc123");

        mockMvc.perform(post("/")
                        .param("url", "https://example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("copyToClipboard")))
                .andExpect(content().string(containsString("navigator.clipboard")));
    }
}
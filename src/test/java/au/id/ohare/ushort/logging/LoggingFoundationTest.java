package au.id.ohare.ushort.logging;

import au.id.ohare.ushort.service.LoggingService;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LoggingFoundationTest {

    @Autowired
    private LoggingService loggingService;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(LoggingService.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @Test
    void applicationStartup_ShouldLogInfoMessage() {
        // Test that application startup logs structured INFO message
        loggingService.logApplicationStartup("1.0.0", "development");
        
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        
        ILoggingEvent logEvent = logsList.get(0);
        assertThat(logEvent.getLevel()).isEqualTo(Level.INFO);
        assertThat(logEvent.getFormattedMessage()).contains("Application started");
        assertThat(logEvent.getFormattedMessage()).contains("version=1.0.0");
        assertThat(logEvent.getFormattedMessage()).contains("profile=development");
    }

    @Test
    void urlCreation_ShouldLogStructuredInfo() {
        // Test that URL creation logs structured INFO with key-value pairs
        loggingService.logUrlCreation("abc123", "https://example.com", 15);
        
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        
        ILoggingEvent logEvent = logsList.get(0);
        assertThat(logEvent.getLevel()).isEqualTo(Level.INFO);
        assertThat(logEvent.getFormattedMessage()).contains("URL created");
        assertThat(logEvent.getFormattedMessage()).contains("shortCode=abc123");
        assertThat(logEvent.getFormattedMessage()).contains("originalUrl=https://example.com");
        assertThat(logEvent.getFormattedMessage()).contains("duration=15ms");
    }

    @Test
    void urlAccess_ShouldLogStructuredInfo() {
        // Test that URL access logs structured INFO with client details
        loggingService.logUrlAccess("abc123", "192.168.1.1", "Mozilla/5.0");
        
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        
        ILoggingEvent logEvent = logsList.get(0);
        assertThat(logEvent.getLevel()).isEqualTo(Level.INFO);
        assertThat(logEvent.getFormattedMessage()).contains("URL accessed");
        assertThat(logEvent.getFormattedMessage()).contains("shortCode=abc123");
        assertThat(logEvent.getFormattedMessage()).contains("clientIp=192.168.1.1");
        assertThat(logEvent.getFormattedMessage()).contains("userAgent=Mozilla/5.0");
    }

    @Test
    void adminAccess_ShouldLogStructuredInfo() {
        // Test that admin access logs structured INFO with user details
        loggingService.logAdminAccess("admin", "sess-123");
        
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        
        ILoggingEvent logEvent = logsList.get(0);
        assertThat(logEvent.getLevel()).isEqualTo(Level.INFO);
        assertThat(logEvent.getFormattedMessage()).contains("Admin accessed analytics");
        assertThat(logEvent.getFormattedMessage()).contains("user=admin");
        assertThat(logEvent.getFormattedMessage()).contains("sessionId=sess-123");
    }

    @Test
    void invalidUrl_ShouldLogWarnMessage() {
        // Test that invalid URLs log WARN level messages
        loggingService.logInvalidUrl("invalid-url", "Missing protocol");
        
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        
        ILoggingEvent logEvent = logsList.get(0);
        assertThat(logEvent.getLevel()).isEqualTo(Level.WARN);
        assertThat(logEvent.getFormattedMessage()).contains("Invalid URL provided");
        assertThat(logEvent.getFormattedMessage()).contains("url=invalid-url");
        assertThat(logEvent.getFormattedMessage()).contains("reason=Missing protocol");
    }

    @Test
    void expiredUrl_ShouldLogWarnMessage() {
        // Test that expired URL access logs WARN level messages
        loggingService.logExpiredUrlAccess("expired123", "2024-01-01", 90);
        
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        
        ILoggingEvent logEvent = logsList.get(0);
        assertThat(logEvent.getLevel()).isEqualTo(Level.WARN);
        assertThat(logEvent.getFormattedMessage()).contains("Expired URL accessed");
        assertThat(logEvent.getFormattedMessage()).contains("shortCode=expired123");
        assertThat(logEvent.getFormattedMessage()).contains("lastAccessed=2024-01-01");
        assertThat(logEvent.getFormattedMessage()).contains("ttlDays=90");
    }

    @Test
    void systemError_ShouldLogErrorMessage() {
        // Test that system failures log ERROR level messages with exception details
        Exception testException = new RuntimeException("Database connection failed");
        loggingService.logSystemError("https://example.com", testException.getMessage(), testException);
        
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        
        ILoggingEvent logEvent = logsList.get(0);
        assertThat(logEvent.getLevel()).isEqualTo(Level.ERROR);
        assertThat(logEvent.getFormattedMessage()).contains("Failed to shorten URL");
        assertThat(logEvent.getFormattedMessage()).contains("originalUrl=https://example.com");
        assertThat(logEvent.getFormattedMessage()).contains("error=Database connection failed");
        assertThat(logEvent.getThrowableProxy()).isNotNull();
    }

    @Test
    void databaseOperation_ShouldLogDebugMessage() {
        // Test that database operations log DEBUG level messages
        loggingService.logDatabaseOperation("url_entity", "SELECT", 5);
        
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        
        ILoggingEvent logEvent = logsList.get(0);
        assertThat(logEvent.getLevel()).isEqualTo(Level.DEBUG);
        assertThat(logEvent.getFormattedMessage()).contains("Database query executed");
        assertThat(logEvent.getFormattedMessage()).contains("table=url_entity");
        assertThat(logEvent.getFormattedMessage()).contains("operation=SELECT");
        assertThat(logEvent.getFormattedMessage()).contains("duration=5ms");
    }

    @Test
    void securityEvent_ShouldLogWarnMessage() {
        // Test that security events log WARN level messages
        loggingService.logAuthenticationFailure("invalid-user", "192.168.1.100", "Bad credentials");
        
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        
        ILoggingEvent logEvent = logsList.get(0);
        assertThat(logEvent.getLevel()).isEqualTo(Level.WARN);
        assertThat(logEvent.getFormattedMessage()).contains("Authentication failed");
        assertThat(logEvent.getFormattedMessage()).contains("username=invalid-user");
        assertThat(logEvent.getFormattedMessage()).contains("ip=192.168.1.100");
        assertThat(logEvent.getFormattedMessage()).contains("reason=Bad credentials");
    }

    @Test
    void metricsLogging_ShouldLogStructuredInfo() {
        // Test that performance metrics log structured INFO messages
        loggingService.logPerformanceMetrics("/shorten", 45, 200);
        
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        
        ILoggingEvent logEvent = logsList.get(0);
        assertThat(logEvent.getLevel()).isEqualTo(Level.INFO);
        assertThat(logEvent.getFormattedMessage()).contains("Performance metrics");
        assertThat(logEvent.getFormattedMessage()).contains("endpoint=/shorten");
        assertThat(logEvent.getFormattedMessage()).contains("responseTime=45ms");
        assertThat(logEvent.getFormattedMessage()).contains("status=200");
    }
}
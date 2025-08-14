# URL Shortener Specification

## Overview
The URL Shortener is a Java Spring Boot application that provides functionality to shorten URLs and manage them. It uses H2 as an in-memory data store and runs on Linux. The application includes both a web interface and an API for creating and managing shortened URLs. It also features an Admin page for analytics, protected by basic authentication. Note that all data will be lost when the application shuts down.

---

## Functional Requirements

1. **Shortened URL Creation**:
   - Users can input a URL via a web page or API.
   - The application generates a shortened URL by hashing the original URL and encoding it using Base62.
   - Shortened URLs will not contain rude words.

2. **Shortened URL Redirection**:
   - Accessing a shortened URL redirects the user to the original URL.
   - If the shortened URL is not found, a generic error webpage is displayed with a link back to the home page.

3. **Admin Analytics**:
   - Admins can view:
     - Total number of shortened URLs stored.
     - Number of times each shortened URL has been accessed.
   - Admin page is protected by basic authentication.

4. **URL Expiry**:
   - Shortened URLs have a TTL of 90 days since last accessed.
   - URLs exceeding the TTL are automatically deleted.

5. **Logging**:
   - Use SLF4J API with Logback (Spring Boot default) for all logging.
   - Use Lombok's @Slf4j annotation for clean logger injection.
   - Application logs access and events with structured logging format.
   - Log levels: INFO (normal operations), WARN (invalid/expired URLs), ERROR (system failures), DEBUG (development).
   - Key events to log:
     - URL creation: `log.info("URL created: shortCode={}, originalUrl={}", shortCode, originalUrl)`
     - URL access: `log.info("URL accessed: shortCode={}, clientIp={}", shortCode, clientIp)`
     - Admin access: `log.info("Admin accessed analytics: user={}", username)`
     - Errors: `log.error("Failed to shorten URL: originalUrl={}, error={}", originalUrl, e.getMessage())`

---

## Non-Functional Requirements

1. **Scalability**:
   - The application must support up to 1,000,000 shortened URLs.

2. **Performance**:
   - Redirection should occur within 100ms under normal load.

3. **Security**:
   - Admin page is protected using Spring Security with basic authentication.
   - Admin credentials configured via environment variables for production security.
   - Default development credentials: admin/admin123 (with warning log).
   - Passwords must be BCrypt encoded for secure storage.
   - Input validation is required to prevent malicious URLs.
   - Configuration examples:
     - Environment: `ADMIN_USERNAME=admin ADMIN_PASSWORD=secure-password`
     - Properties: `admin.username=${ADMIN_USERNAME:admin}` and `admin.password=${ADMIN_PASSWORD:admin123}`

4. **Styling**:
   - The web interface uses Bootstrap's default CSS styling.

5. **Error Handling**:
   - Generic error webpage is displayed for invalid or expired URLs.

---

## Technical Requirements

1. **Backend**:
   - Java Spring Boot application.
   - H2 in-memory database for storing URLs and analytics.

2. **Frontend**:
   - Mustache templates for server-side rendering.
   - Bootstrap for styling.
   - Web pages for URL creation, error handling, and Admin analytics.

3. **API**:
   - RESTful API for creating and managing shortened URLs.
   - Endpoints:
     - `POST /shorten` - Create a shortened URL.
     - `GET /{shortenedUrl}` - Redirect to the original URL.
     - `GET /admin/analytics` - Retrieve analytics (Admin only).

4. **Deployment**:
   - Runs on Linux.
   - Configurable via environment variables (e.g., TTL, admin credentials).
   - Required production environment variables: `ADMIN_USERNAME`, `ADMIN_PASSWORD`.
   - Optional: `TTL_DAYS` (defaults to 90 days).

5. **Database Schema**:
   - **URLs Table**:
     - `id` (Primary Key)
     - `original_url` (Text)
     - `shortened_url` (Text, unique)
     - `access_count` (Integer)
     - `last_accessed` (Timestamp)
   - **Admin Table** (Optional for authentication):
     - `username` (Text, unique)
     - `password` (Text, hashed)

6. **URL Shortening Algorithm**:
   - Use SHA-256 to hash the original URL.
   - Take the first 6-8 bytes of the hash.
   - Encode the bytes using Base62 (0-9, a-z, A-Z) for URL-friendly shortened codes.
   - Implement a filter to avoid rude words in the generated shortened URL.

---

## Additional Considerations

1. **Testing** (Test-Driven Development):
   - Follow TDD approach: write tests first, then implement functionality to make tests pass.
   - Unit tests for backend logic using JUnit 5.
   - Integration tests for API endpoints using Spring Boot Test with TestRestTemplate.
   - UI tests for web pages using MockMvc.
   - Red-Green-Refactor cycle: write failing test, make it pass, then refactor.

2. **Monitoring**:
   - Add metrics for monitoring application performance and errors.

3. **Documentation**:
   - Provide API documentation (e.g., Swagger/OpenAPI).
   - Include deployment instructions.

4. **Future Enhancements**:
   - Support for custom shortened URLs.
   - OAuth-based authentication for Admin page.

---
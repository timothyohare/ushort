# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot URL shortener application called "ushort" that provides functionality to shorten URLs and manage them. The application uses H2 as an in-memory data store and includes both a web interface and an API for creating and managing shortened URLs, plus an Admin page for analytics protected by basic authentication. Note that all data is lost when the application shuts down.

**Key Documentation:**
- `specification.md` - Complete functional and technical requirements
- `blueprint.md` - Step-by-step implementation plan with TDD approach

## Architecture

- **Backend**: Java 21 Spring Boot 3.5.4 application
- **Database**: H2 in-memory database
- **Frontend**: Mustache templates with Bootstrap CSS
- **Testing**: JUnit 5 with Testcontainers support
- **Build**: Gradle with wrapper

### Key Dependencies
- Spring Boot 3.5.4 with Dependency Management 1.1.7
- Spring Boot Starter Web (REST API and MVC)
- Spring Boot Starter Mustache (templating)
- Spring Boot Starter Actuator (monitoring)
- Spring Boot DevTools (development hot reload)
- Spring Boot Docker Compose (development services)
- H2 Database (runtime - in-memory database)
- Lombok (compile/annotation processing - reducing boilerplate)
- Spring Boot Starter Test (testing - includes JUnit 5)
- Spring Boot Testcontainers (testing)
- Testcontainers JUnit Jupiter (testing)
- JUnit Platform Launcher (test runtime)

## Common Development Commands

### Build and Run
```bash
# Build the application
./gradlew build

# Run the application
./gradlew bootRun

# Run tests
./gradlew test

# Run a specific test class
./gradlew test --tests "au.id.ohare.ushort.UshortApplicationTests"

# Clean build
./gradlew clean build
```

### Docker Compose
```bash
# Start services (when compose.yaml is configured)
docker-compose up -d

# Stop services
docker-compose down
```

## Project Structure

- `src/main/java/au/id/ohare/ushort/` - Main application code
- `src/main/resources/` - Application configuration and static resources
- `src/test/java/au/id/ohare/ushort/` - Test classes
- `build.gradle` - Gradle build configuration
- `compose.yaml` - Docker Compose configuration (currently empty)

## Implementation Status

This appears to be a newly initialized Spring Boot project based on the specification. The main components still need to be implemented:

- URL shortening logic with SHA256 hashing
- Database schema for URLs and admin tables
- REST API endpoints (`POST /shorten`, `GET /{shortenedUrl}`, `GET /admin/analytics`)
- Web pages for URL creation and error handling
- Admin analytics page with basic authentication
- URL expiry mechanism (90-day TTL)

## Key Features to Implement

1. **URL Shortening**: Hash-based shortened URLs avoiding rude words
2. **Redirection**: Fast redirection (target: <100ms) to original URLs
3. **Analytics**: Admin dashboard showing total URLs and access counts
4. **Expiry**: Automatic cleanup of URLs not accessed for 90 days
5. **Security**: Basic authentication for admin features
6. **Scale**: Support up to 1,000,000 shortened URLs

## Development Approach

This project follows **Test-Driven Development (TDD)**:
- Write tests first to define expected behavior
- Implement minimal code to make tests pass
- Refactor while keeping tests green
- Follow Red-Green-Refactor cycle

See `blueprint.md` for detailed TDD implementation chunks and `specification.md` for comprehensive testing strategy.

## Logging Standards

Use **SLF4J with Logback** (Spring Boot default):
- Use Lombok's `@Slf4j` annotation for clean logger injection
- Structured logging with key-value pairs: `log.info("Event occurred: key={}", value)`
- Log levels: INFO (normal ops), WARN (invalid URLs), ERROR (failures), DEBUG (development)
- Key events: URL creation, access, admin operations, errors

## Security Configuration

**Admin Authentication:**
- Use Spring Security with BCrypt password encoding
- Environment variables for production: `ADMIN_USERNAME`, `ADMIN_PASSWORD`
- Development defaults: `admin/admin123` (logs warning)
- Configuration pattern:
  ```properties
  admin.username=${ADMIN_USERNAME:admin}
  admin.password=${ADMIN_PASSWORD:admin123}
  ```

## Configuration

The application uses standard Spring Boot configuration via `application.properties`. Key areas that will need configuration:
- Admin authentication credentials (environment variables)
- TTL settings (`TTL_DAYS`, defaults to 90)
- Logging levels and patterns
- H2 console access (for development)

## Algorithm Details

URL Shortening (from `specification.md`):
1. SHA-256 hash the original URL
2. Take first 6-8 bytes of hash
3. Encode using Base62 (0-9, a-z, A-Z)
4. Filter out rude words from generated codes
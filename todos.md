# URL Shortener Development Todos

This file tracks the progress of implementing the URL shortener project following the Test-Driven Development (TDD) approach outlined in `blueprint.md`.

## Progress Overview
- [x] 2/9 chunks completed
- [x] Project started - Chunks 1-2 complete!

## Chunk 1: Project Setup
**Status: ✅ COMPLETED**
- [x] Create a Spring Boot application
- [x] Configure H2 in-memory database
- [x] Verify the setup with a simple "Hello World" endpoint

## Chunk 2: URL Shortening Logic (TDD)
**Status: ✅ COMPLETED**
- [x] Write unit tests using JUnit 5 to define expected behavior of hashing logic
- [x] Implement SHA256 + Base62 encoding mechanism to make tests pass
- [x] Implement rude word filtering
- [x] Refactor implementation while keeping tests green

## Chunk 3: Database Schema (TDD)
**Status: Not Started**
- [ ] Write integration tests using Spring Boot Test to define expected database operations
- [ ] Design and implement URLs table (id, original_url, shortened_url, access_count, last_accessed)
- [ ] Design and implement Admin table (username, password_hash)
- [ ] Create JPA entities and repositories
- [ ] Refactor schema design while keeping tests green

## Chunk 4: API Development (TDD)
**Status: Not Started**
- [ ] Write integration tests using TestRestTemplate to define expected API behavior
- [ ] Develop `POST /shorten` endpoint to make tests pass
- [ ] Develop `GET /{shortenedUrl}` redirection endpoint to make tests pass
- [ ] Integrate URL expiry checks in redirection logic
- [ ] Add comprehensive logging for API operations
- [ ] Refactor API implementation while keeping tests green

## Chunk 5: Frontend Development (TDD)
**Status: Not Started**
- [ ] Write UI tests using MockMvc to define expected web page behavior
- [ ] Build URL creation page using Mustache templates to make tests pass
- [ ] Build error handling page using Mustache templates to make tests pass
- [ ] Integrate Bootstrap for styling
- [ ] Refactor templates while keeping tests green

## Chunk 6: Admin Analytics (TDD)
**Status: Not Started**
- [ ] Write integration tests using TestRestTemplate to define expected Admin functionality
- [ ] Create Admin analytics page using Mustache templates to make tests pass
- [ ] Implement Spring Security configuration with BCrypt password encoding
- [ ] Configure admin credentials via environment variables (ADMIN_USERNAME, ADMIN_PASSWORD)
- [ ] Set default development credentials (admin/admin123) with warning log
- [ ] Protect Admin page with basic authentication to make tests pass
- [ ] Add comprehensive logging for admin operations and security events
- [ ] Refactor Admin implementation while keeping tests green

## Chunk 7: Logging Foundation (TDD)
**Status: Not Started**
- [ ] Write JUnit 5 tests to define expected logging behavior using SLF4J/Logback
- [ ] Add structured logging foundation using Lombok's @Slf4j annotation
- [ ] Implement basic logging for application startup and core operations
- [ ] Use appropriate log levels (INFO, WARN, ERROR, DEBUG) to make tests pass
- [ ] Refactor logging implementation while keeping tests green

## Chunk 8: URL Expiry Mechanism (TDD)
**Status: Not Started**
- [ ] Write unit tests using JUnit 5 to define expected expiry behavior
- [ ] Implement TTL logic for deleting expired URLs (90-day default) to make tests pass
- [ ] Integrate expiry checks with URL access operations
- [ ] Add logging for expiry operations
- [ ] Refactor expiry mechanism while keeping tests green

## Chunk 9: Integration Testing & Deployment
**Status: Not Started**
- [ ] Write end-to-end integration tests covering complete user workflows
- [ ] Test entire URL lifecycle: creation, access, expiry, and admin analytics
- [ ] Verify logging output across all operations
- [ ] Configure application to run on Linux
- [ ] Set up environment variables for configuration (ADMIN_USERNAME, ADMIN_PASSWORD, TTL_DAYS)
- [ ] Create deployment documentation with security best practices
- [ ] Verify deployment with test environment

## Development Guidelines

### TDD Approach
Follow the Red-Green-Refactor cycle for each chunk:
1. **Red**: Write failing tests that define expected behavior
2. **Green**: Write minimal code to make tests pass
3. **Refactor**: Improve code while keeping tests green

### Key Technologies
- **Backend**: Java 21, Spring Boot 3.5.4
- **Database**: H2 in-memory
- **Frontend**: Mustache templates, Bootstrap CSS
- **Testing**: JUnit 5, TestRestTemplate, MockMvc, Testcontainers
- **Security**: Spring Security with BCrypt
- **Logging**: SLF4J with Logback, Lombok @Slf4j
- **Build**: Gradle

### Completion Criteria
Each chunk is considered complete when:
- [ ] All tests are passing (green)
- [ ] Code meets functional requirements
- [ ] Implementation follows TDD principles
- [ ] Logging is properly implemented
- [ ] Code is refactored and clean

## Notes
- All data will be lost when application shuts down (H2 in-memory)
- Default admin credentials: admin/admin123 (development only)
- Production requires ADMIN_USERNAME and ADMIN_PASSWORD environment variables
- URL expiry default: 90 days since last access
- Target redirection performance: <100ms
- Support up to 1,000,000 shortened URLs

---
*Last updated: 2025-08-14 - Chunk 1 completed*
# Blueprint for Building the URL Shortener Project

## Step-by-Step Plan

### Setup the Project
- Create a Spring Boot application.
- Configure H2 as the in-memory database.
- Set up the project structure for backend, frontend, and API.

### Implement URL Shortening Logic
- Create a hashing mechanism for generating shortened URLs.
- Ensure the hash avoids rude words.

### Database Integration
- Design and implement the database schema.
- Create tables for storing URLs and admin credentials.

### API Development
- Develop RESTful endpoints for URL creation and redirection.
- Implement error handling for invalid or expired URLs.

### Frontend Development
- Build web pages for URL creation and error handling using Mustache templates.
- Use Bootstrap for styling.

### Admin Analytics
- Create an Admin page for viewing analytics.
- Protect the Admin page with basic authentication.

### URL Expiry Mechanism
- Implement TTL logic for deleting expired URLs.

### Logging
- Add logging for access and application events.

### Testing
- Write unit tests for backend logic using JUnit 5.
- Write integration tests for API endpoints using Spring Boot Test with TestRestTemplate.
- Write UI tests for web pages using MockMvc.

### Deployment
- Configure the application to run on Linux.
- Set up environment variables for configuration.

---

## Iterative Chunks

### Chunk 1: Project Setup
- Create a Spring Boot application.
- Configure H2 in-memory database.
- Verify the setup with a simple "Hello World" endpoint.

### Chunk 2: URL Shortening Logic (TDD)
- Write unit tests using JUnit 5 to define the expected behavior of the hashing logic.
- Implement a hashing mechanism for generating shortened URLs to make tests pass.
- Refactor the implementation while keeping tests green.

### Chunk 3: Database Schema (TDD)
- Write integration tests using Spring Boot Test to define expected database operations.
- Design and implement the database schema to make tests pass.
- Create tables for URLs and admin credentials.
- Refactor schema design while keeping tests green.

### Chunk 4: API Development (TDD)
- Write integration tests using TestRestTemplate to define expected API behavior.
- Develop the `POST /shorten` endpoint for creating shortened URLs to make tests pass.
- Develop the `GET /{shortenedUrl}` endpoint for redirection to make tests pass.
- Integrate URL expiry checks in redirection logic.
- Add comprehensive logging for API operations.
- Refactor API implementation while keeping tests green.

### Chunk 5: Frontend Development (TDD)
- Write UI tests using MockMvc to define expected web page behavior.
- Build the web page for URL creation using Mustache templates to make tests pass.
- Build the error handling page using Mustache templates to make tests pass.
- Use Bootstrap for styling.
- Refactor templates while keeping tests green.

### Chunk 6: Admin Analytics (TDD)
- Write integration tests using TestRestTemplate to define expected Admin functionality.
- Create the Admin page for viewing analytics to make tests pass.
- Implement Spring Security configuration with BCrypt password encoding.
- Configure admin credentials via environment variables (ADMIN_USERNAME, ADMIN_PASSWORD).
- Set default development credentials (admin/admin123) with warning log.
- Protect the Admin page with basic authentication to make tests pass.
- Add comprehensive logging for admin operations and security events.
- Refactor Admin implementation while keeping tests green.

### Chunk 7: Logging Foundation (TDD)
- Write JUnit 5 tests to define expected logging behavior using SLF4J/Logback.
- Add structured logging foundation using Lombok's @Slf4j annotation.
- Implement basic logging for application startup and core operations.
- Use appropriate log levels (INFO, WARN, ERROR, DEBUG) to make tests pass.
- Refactor logging implementation while keeping tests green.

### Chunk 8: URL Expiry Mechanism (TDD)
- Write unit tests using JUnit 5 to define expected expiry behavior.
- Implement TTL logic for deleting expired URLs to make tests pass.
- Integrate expiry checks with URL access operations.
- Add logging for expiry operations.
- Refactor expiry mechanism while keeping tests green.

### Chunk 9: Integration Testing & Deployment
- Write end-to-end integration tests covering complete user workflows.
- Test entire URL lifecycle: creation, access, expiry, and admin analytics.
- Verify logging output across all operations.
- Configure the application to run on Linux.
- Set up environment variables for configuration.
- Create deployment documentation with security best practices.
- Verify deployment with a test environment.

---

## Prompts for Code-Generation LLM

### Prompt 1: Project Setup
```text
Create a new Spring Boot application with H2 as the in-memory database. Add a simple "Hello World" endpoint to verify the setup. Ensure the application runs on Linux. Note that data will be lost when the application shuts down.
```

### Prompt 2: URL Shortening Logic
```text
Using TDD approach: First write unit tests using JUnit 5 to define expected behavior of the URL shortening algorithm. Then implement a hashing mechanism using SHA256 and Base62 encoding that makes the tests pass. Ensure the hash avoids rude words. Follow Red-Green-Refactor cycle.
```

### Prompt 3: Database Schema
```text
Design and implement the database schema for storing URLs and admin credentials. Create tables with the following structure:
- URLs Table: id (Primary Key), original_url (Text), shortened_url (Text, unique), access_count (Integer), last_accessed (Timestamp).
- Admin Table: username (Text, unique), password (Text, hashed).
Using TDD approach: First write integration tests using Spring Boot Test to define expected database behavior. Then implement the database schema and operations to make tests pass. Follow Red-Green-Refactor cycle.
```

### Prompt 4: API Development
```text
Develop the following RESTful API endpoints:
- `POST /shorten`: Accepts a URL and returns a shortened URL.
- `GET /{shortenedUrl}`: Redirects to the original URL or shows an error page if the URL is invalid or expired.
Using TDD approach: First write integration tests using TestRestTemplate to define expected API behavior. Then develop the endpoints to make tests pass. Follow Red-Green-Refactor cycle.
```

### Prompt 5: Frontend Development
```text
Build the following web pages using Mustache templates:
- URL creation page: Allows users to input a URL and receive a shortened URL.
- Error handling page: Displays a generic error message with a link back to the home page.
Use Bootstrap for styling. Using TDD approach: First write UI tests using MockMvc to define expected web page behavior. Then build the pages to make tests pass. Follow Red-Green-Refactor cycle.
```

### Prompt 6: Admin Analytics
```text
Create an Admin page using Mustache templates that displays:
- Total number of shortened URLs stored.
- Number of times each shortened URL has been accessed.
Implement Spring Security with BCrypt password encoding. Configure admin credentials via environment variables (ADMIN_USERNAME, ADMIN_PASSWORD) with defaults (admin/admin123) for development. Log warnings when using default credentials. Using TDD approach: First write integration tests using TestRestTemplate to define expected Admin behavior including authentication. Then implement Admin functionality and security configuration to make tests pass. Follow Red-Green-Refactor cycle.
```

### Prompt 7: URL Expiry Mechanism
```text
Implement TTL logic for deleting expired URLs. Shortened URLs should expire 90 days after their last access. Using TDD approach: First write unit tests using JUnit 5 to define expected expiry behavior. Then implement TTL logic to make tests pass. Follow Red-Green-Refactor cycle.
```

### Prompt 8: Logging
```text
Add structured logging using SLF4J with Logback and Lombok's @Slf4j annotation. Log key events: URL creation, access, admin operations, and errors. Use standard error levels (INFO, WARN, ERROR, DEBUG). Using TDD approach: First write JUnit 5 tests to define expected logging behavior with structured format. Then implement logging to make tests pass. Follow Red-Green-Refactor cycle.
```

### Prompt 7: Logging Foundation
```text
Using TDD approach: First write JUnit 5 tests to define expected logging behavior using SLF4J with Logback and Lombok's @Slf4j annotation. Implement structured logging foundation for application startup and core operations. Use standard error levels (INFO, WARN, ERROR, DEBUG). Follow Red-Green-Refactor cycle.
```

### Prompt 8: URL Expiry Mechanism
```text
Implement TTL logic for deleting expired URLs. Shortened URLs should expire 90 days after their last access. Integrate expiry checks with URL access operations. Add logging for expiry operations. Using TDD approach: First write unit tests using JUnit 5 to define expected expiry behavior. Then implement TTL logic to make tests pass. Follow Red-Green-Refactor cycle.
```

### Prompt 9: Integration Testing & Deployment
```text
Write end-to-end integration tests covering complete user workflows and URL lifecycle. Configure the application to run on Linux. Set up environment variables for configuration:
- ADMIN_USERNAME and ADMIN_PASSWORD (required for production)
- TTL_DAYS (optional, defaults to 90 days)
Provide deployment instructions including security best practices. Verify deployment with a test environment. Note that the H2 in-memory database will reset on each application restart.
```

---
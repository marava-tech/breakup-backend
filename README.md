# Breakup Backend

Spring Boot API for the Breakup Stories platform.

## What It Does

- Email OTP authentication and JWT sessions.
- Story submission and management.
- AI rewrite/enhancement workflow support.
- Text-to-speech/audio generation integration.
- MongoDB persistence.
- Redis caching/workflow support.
- Cloud storage integration.
- Swagger/OpenAPI documentation.

## Stack

- Java 21.
- Spring Boot 3.2.
- MongoDB.
- Redis.
- Spring Security.
- Google Cloud speech/TTS/storage libraries.

## Local Development

```bash
mvn clean install
mvn spring-boot:run
```

## Configuration

Use environment variables or local ignored files for secrets:

```env
MONGODB_URI=replace-with-mongodb-uri
JWT_SECRET=replace-with-secure-secret
EMAIL_USERNAME=replace-with-email
EMAIL_PASSWORD=replace-with-app-password
REDIS_URL=replace-with-redis-url
GOOGLE_APPLICATION_CREDENTIALS=replace-with-local-path
```

## Verify

```bash
mvn test
mvn clean package
```

## Safety Rules

- Do not commit `.env*`, service account JSON, API keys, DB credentials, or storage credentials.
- Do not restart shared MongoDB or Redis unless explicitly asked.
- Verify audio/story processing manually after AI or TTS changes.


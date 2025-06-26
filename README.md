# Breakup Stories Backend API

A clean and modular Spring Boot 3.2 application for managing breakup stories with MongoDB persistence, featuring JWT authentication and comprehensive user management.

## 🚀 Tech Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data MongoDB**
- **Spring Security**
- **JWT Authentication**
- **Lombok**
- **Spring Web**
- **Spring Validation**
- **Spring Boot DevTools**
- **Swagger/OpenAPI 3**

## 📂 Project Structure

```
src/main/java/com/breakupstories/
├── BreakupStoriesApplication.java
├── config/
│   ├── MongoConfig.java
│   ├── OpenApiConfig.java
│   ├── SecurityConfig.java
│   ├── WebConfig.java
│   └── JwtAuthenticationFilter.java
├── controller/
│   ├── AuthController.java
│   ├── StoryController.java
│   ├── UserController.java
│   ├── FeedbackController.java
│   └── AuditController.java
├── dto/
│   ├── AuthResponse.java
│   ├── CreateStoryRequest.java
│   ├── PagedResponse.java
│   ├── StoryResponse.java
│   ├── UserRequest.java
│   ├── UserResponse.java
│   ├── FeedbackRequest.java
│   ├── FeedbackResponse.java
│   ├── AuditRequest.java
│   └── AuditResponse.java
├── model/
│   ├── Audit.java
│   ├── Bookmark.java
│   ├── Comment.java
│   ├── Content.java
│   ├── Emotion.java
│   ├── Feedback.java
│   ├── Keyword.java
│   ├── Like.java
│   ├── Story.java
│   └── User.java
├── repository/
│   ├── AuditRepository.java
│   ├── BookmarkRepository.java
│   ├── CommentRepository.java
│   ├── FeedbackRepository.java
│   ├── LikeRepository.java
│   ├── StoryRepository.java
│   └── UserRepository.java
└── service/
    ├── JwtService.java
    ├── StoryService.java
    ├── UserService.java
    ├── FeedbackService.java
    └── AuditService.java
```

## 🏗️ Features

### Core Entities
- **User**: Authentication and profile management
- **Story**: Main content with audio, text, images, and metadata
- **Content**: Flexible content types (TEXT, IMAGE, VIDEO)
- **Emotion**: Emotional analysis with scores
- **Keyword**: Tagged keywords for categorization
- **Like**: User story interactions
- **Bookmark**: User story bookmarks
- **Comment**: Nested comment system with replies
- **Feedback**: User feedback with different tones
- **Audit**: Change tracking and logging

### Authentication & Security
- **JWT Tokens**: Stateless authentication with JWT
- **Spring Security**: Comprehensive security configuration
- **OTP-based Authentication**: Email-based OTP verification for secure login and registration

### API Endpoints

#### Authentication
- `POST /api/auth/send-otp-registration` - Send OTP for new user registration
- `POST /api/auth/send-otp-login` - Send OTP for existing user login
- `POST /api/auth/verify-otp-registration` - Verify OTP and create new user account
- `POST /api/auth/verify-otp-login` - Verify OTP and login existing user
- `GET /api/auth/me` - Get current user information
- `POST /api/auth/refresh` - Refresh JWT token

#### Users
- `GET /api/users` - Get paginated users
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/email/{email}` - Get user by email
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

#### Stories
- `POST /api/stories` - Create a new story (Authenticated)
- `GET /api/stories` - Get paginated stories
- `GET /api/stories/{id}` - Get story by ID

#### Feedbacks
- `POST /api/feedbacks` - Create a new feedback (Authenticated)
- `GET /api/feedbacks` - Get paginated feedbacks
- `GET /api/feedbacks/story/{storyId}` - Get feedbacks by story
- `GET /api/feedbacks/user/{userId}` - Get feedbacks by user
- `GET /api/feedbacks/{feedbackId}` - Get feedback by ID
- `PUT /api/feedbacks/{feedbackId}` - Update feedback (Owner only)
- `DELETE /api/feedbacks/{feedbackId}` - Delete feedback (Owner only)

#### Audits
- `POST /api/audits` - Create a new audit entry
- `GET /api/audits` - Get paginated audit entries
- `GET /api/audits/user/{userId}` - Get audits by user
- `GET /api/audits/entity-type/{entityType}` - Get audits by entity type
- `GET /api/audits/entity/{entityId}` - Get audits by entity ID
- `GET /api/audits/{auditId}` - Get audit by ID
- `DELETE /api/audits/{auditId}` - Delete audit entry

## 🛠️ Setup Instructions

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- MongoDB 4.4+ (or Docker)

### Environment Variables
Create a `.env` file or set environment variables:
```bash
JWT_SECRET=your-super-secret-jwt-key
GMAIL_USERNAME=your-email@gmail.com
GMAIL_APP_PASSWORD=your-16-character-app-password
```

**Note**: For email configuration, see `EMAIL_SETUP.md` for detailed instructions on setting up Gmail for OTP emails.

### Running with Docker MongoDB
```bash
# Start MongoDB
docker run -d --name mongodb -p 27017:27017 mongo:latest

# Build and run the application
mvn clean install
mvn spring-boot:run
```

### Running Locally
1. **Install MongoDB** locally or use Docker
2. **Clone the repository**
3. **Configure email settings** (see `EMAIL_SETUP.md`)
4. **Build the project**:
   ```bash
   mvn clean install
   ```
5. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

## 📖 API Documentation

Once the application is running, you can access:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

## 🔐 Authentication Flow

### OTP-based Authentication
1. **Registration Flow**:
   - User calls `/api/auth/send-otp-registration` with email
   - System sends OTP to email
   - User calls `/api/auth/verify-otp-registration` with OTP and user details
   - System creates user account and returns JWT token

2. **Login Flow**:
   - User calls `/api/auth/send-otp-login` with email
   - System sends OTP to email
   - User calls `/api/auth/verify-otp-login` with OTP
   - System returns JWT token

3. **JWT Token Usage**:
   - Frontend stores JWT token and uses it for authenticated requests
   - Include JWT token in Authorization header: `Authorization: Bearer <your-jwt-token>`

### Test Bypass Authentication (Development Only)
For testing purposes, you can bypass OTP authentication using special headers:

- **X-BS-Authorization**: Set to `true` to enable test bypass
- **X-BS-UserId**: Provide the user ID to authenticate as

**Example**:
```bash
curl -H "X-BS-Authorization: true" \
     -H "X-BS-UserId: user-id-here" \
     http://localhost:8080/api/auth/me
```

**Note**: This feature should only be used in development/testing environments and should be disabled in production.

### Admin Authorization (Development Only)
For testing admin-only endpoints, you can use the admin authorization header:

- **X-BS-Admin**: Set to `true` to grant admin privileges to the authenticated user

**Example**:
```bash
# Access admin-only endpoints with admin privileges
curl -H "X-BS-Authorization: true" \
     -H "X-BS-UserId: user-id-here" \
     -H "X-BS-Admin: true" \
     http://localhost:8080/api/audits

# Or with JWT token
curl -H "Authorization: Bearer <your-jwt-token>" \
     -H "X-BS-Admin: true" \
     http://localhost:8080/api/users
```

**Admin-only Endpoints**:
- All `/api/audits/*` endpoints
- All `/api/configs/*` endpoints  
- All `/api/users/*` endpoints

**Note**: This feature should only be used in development/testing environments and should be disabled in production.

### JWT Token Usage
Include the JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## 🧪 Testing

Run the tests with:
```bash
mvn test
```

## 📝 Example Usage

### Registration Flow
```bash
# 1. Send OTP for registration
curl -X POST http://localhost:8080/api/auth/send-otp-registration \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com"}'

# 2. Verify OTP and create account
curl -X POST http://localhost:8080/api/auth/verify-otp-registration \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "otp": "123456",
    "name": "John Doe",
    "gender": "Male",
    "age": 25
  }'
```

### Login Flow
```bash
# 1. Send OTP for login
curl -X POST http://localhost:8080/api/auth/send-otp-login \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com"}'

# 2. Verify OTP and login
curl -X POST http://localhost:8080/api/auth/verify-otp-login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "otp": "123456"
  }'
```

### Create a Story (with JWT)
```bash
curl -X POST http://localhost:8080/api/stories \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -d '{
    "title": "My Breakup Story",
    "audioUrl": "https://example.com/audio.mp3",
    "contents": [
      {
        "type": "TEXT",
        "data": "This is my story...",
        "orderIndex": 1
      }
    ],
    "tags": ["breakup", "healing"],
    "emotions": [
      {
        "type": "SAD",
        "score": 0.8
      }
    ]
  }'
```

### Get Current User
```bash
curl -H "Authorization: Bearer <your-jwt-token>" \
  http://localhost:8080/api/auth/me
```

### Update User
```bash
curl -X PUT http://localhost:8080/api/users/{userId} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -d '{
    "name": "John Doe Updated",
    "email": "john@example.com",
    "gender": "Male",
    "age": 26
  }'
```

### Test Bypass Authentication (Development Only)
```bash
# Get current user using test bypass
curl -H "X-BS-Authorization: true" \
     -H "X-BS-UserId: user-id-here" \
     http://localhost:8080/api/auth/me

# Create a story using test bypass
curl -X POST http://localhost:8080/api/stories \
  -H "Content-Type: application/json" \
  -H "X-BS-Authorization: true" \
  -H "X-BS-UserId: user-id-here" \
  -d '{
    "title": "Test Story",
    "contents": [
      {
        "type": "TEXT",
        "data": "This is a test story",
        "orderIndex": 1
      }
    ]
  }'

# Access admin endpoints using admin authorization
curl -H "X-BS-Authorization: true" \
     -H "X-BS-UserId: user-id-here" \
     -H "X-BS-Admin: true" \
     http://localhost:8080/api/audits

# Get all users with admin privileges
curl -H "X-BS-Authorization: true" \
     -H "X-BS-UserId: user-id-here" \
     -H "X-BS-Admin: true" \
     http://localhost:8080/api/users
```

### Create Feedback
```bash
curl -X POST http://localhost:8080/api/feedbacks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -d '{
    "storyId": "story-id-here",
    "tone": "POSITIVE",
    "contents": [
      {
        "type": "TEXT",
        "data": "This story really helped me heal",
        "orderIndex": 1
      }
    ]
  }'
```

### Get Feedbacks by Story
```bash
curl -H "Authorization: Bearer <your-jwt-token>" \
  "http://localhost:8080/api/feedbacks/story/story-id-here?page=0&size=10"
```

### Create Audit Entry
```bash
curl -X POST http://localhost:8080/api/audits \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -d '{
    "userId": "user-id-here",
    "entityType": "STORY",
    "actionType": "CREATE",
    "entityId": "story-id-here"
  }'
```

### Get Audits by Entity Type
```bash
curl -H "Authorization: Bearer <your-jwt-token>" \
  "http://localhost:8080/api/audits/entity-type/STORY?page=0&size=10"
```

## 🔧 Configuration

The application uses `application.yml` for configuration. Key settings:

- **MongoDB**: Configured for localhost:27017
- **Server**: Runs on port 8080
- **CORS**: Enabled for all origins
- **Swagger**: Available at `/swagger-ui.html`
- **JWT**: Token generation and validation
- **Email**: Gmail SMTP for OTP delivery

## 🏗️ Architecture

The project follows clean architecture principles:

- **Controllers**: Handle HTTP requests and responses
- **Services**: Contain business logic
- **Repositories**: Data access layer
- **DTOs**: Data transfer objects for API contracts
- **Models**: MongoDB document entities
- **Config**: Global configuration and beans
- **Security**: JWT authentication with OTP-based verification

## 📄 License

This project is licensed under the MIT License.
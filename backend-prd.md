# Breakup Stories Backend Product Requirements Document (PRD)

**Version:** 1.0 (Current Implementation)  
**Date:** February 2026

## 1. Product Overview
The Breakup Stories Backend is a robust, modular RESTful API built with Java and Spring Boot. It serves as the core infrastructure for the Breakup Stories platform—a safe space for users to share their breakup experiences, receive AI-powered consoling, and engage with a supportive community. The backend manages authentication, content (stories - audio/text), social interactions, user rewards, and comprehensive admin capabilities.

## 2. Target Audience
- **End Users (Mobile App/Web):** Individuals going through breakups seeking to share stories (audio/text), listen to others, and get consoling.
- **Administrators (Admin Dashboard):** Platform managers who moderate content, manage users, handle withdrawal requests, and configure app settings.

## 3. Tech Stack & Infrastructure
- **Language:** Java 17
- **Framework:** Spring Boot 3.2.0
- **Database:** MongoDB 4.4+ (Spring Data MongoDB)
- **Security:** Spring Security with stateless JWT Authentication
- **Documentation:** Swagger/OpenAPI 3
- **Utilities:** Lombok, Spring Validation, Java Mail Sender (for OTPs)
- **Deployment:** Docker support (Dockerfiles included)

## 4. Key Features & Functional Requirements

### 4.1 Authentication & User Management
*   **OTP-Based Login/Signup:** Passwordless entry via Email OTP.
    *   `POST /api/auth/send-otp-registration`, `POST /api/auth/verify-otp-registration`
    *   `POST /api/auth/send-otp-login`, `POST /api/auth/verify-otp-login`
*   **JWT Security:** Secured endpoints using Bearer tokens. Token refresh mechanism supported.
*   **User Profiles:**
    *   Profile management (Name, Gender, Age, Profile Image).
    *   Public user profiles.
*   **Ban System:**
    *   Pre-login check for banned emails (`/api/public/check-email-ban`).
    *   Device-level banning handled by Admin.

### 4.2 Story Management
*   **Core Entity:** A "Story" can contain mixed media (Audio, Text, Image, Video) but primarily focuses on Audio and Written narratives.
*   **Creation:**
    *   Support for uploading Voice stories (Audio URL).
    *   Support for Written stories.
    *   Metadata: Title, Tags (Keywords), Emotions (with scoring).
*   **Consumption:**
    *   Main Feed (`/api/stories`).
    *   Filtered Feeds: `SIMILAR`, `LANGUAGE` (users preferred language), `NEAR_ME`.
    *   Story Details: Full content retrieval.
*   **Search:** Search functionality for stories.

### 4.3 Social Interactions
*   **Likes:** Users can like/unlike stories.
*   **Bookmarks:** Users can save stories for later. Real-time bookmark status checks.
*   **Comments:**
    *   Users can comment on stories.
    *   Support for threaded replies (data model supports it, endpoint `/api/comments/{commentId}/replies` reserved).
    *   Moderation: Users can flag comments (reserved endpoints).

### 4.4 Rewards & Withdrawals
*   **Coins System:** Users earn coins (logic handled internally/triggered).
*   **Referrals:** Tracking referral stats and device-level referral usage.
*   **Withdrawals:**
    *   Users can request withdrawals of their earnings.
    *   History view implementation.
    *   Admin process/reject workflow.

### 4.5 Feedback System
*   **User Feedback:** Users can submit feedback with specific "tones" (e.g., Positive, Negative).
*   **Admin Response:** Admins can reply to feedback and update status.

### 4.6 AI & Consoling Integration
*   **Consoling Message:** Backend stores/retrieves AI-generated consoling messages for stories (`/api/story/{id}/consoling-message`).
*   **Processing:** Mechanisms to retry AI processing for stories.

### 4.7 App Configuration (Config-Driven UI)
*   **Dynamic Configs:** The app retrieves configurations from the backend to control active features, UI texts, or rules.
    *   Global App Configs (`/api/configs/app-configs`).
    *   User-specific Configs (`/api/configs/user-configs`).
    *   Language support lists.

### 4.8 Admin Dashboard API
*   **Analytics:** Stats for Dashboard, Stories, Users, and Feedback visibility.
*   **Content Moderation:**
    *   View, Edit, Delete, Approve/Reject Stories.
    *   Manage Feedbacks.
*   **User Administration:**
    *   View user lists and details.
    *   Manage Coins (Credit/Invalidate).
    *   Ban/Unban Devices (`/api/admin/banned-devices`).
*   **Health:** System health check endpoints.

## 5. Data Models (Core Entities)
*   **User:** ID, Email, Name, Role, Status, Device Info.
*   **Story:** ID, Title, Status (DRAFT, PUBLISHED, ARCHIVED), Content List (Type, Data, Order), Emotions, Tags.
*   **Interaction:** Like, Bookmark (Links User <-> Story).
*   **Comment:** Content, Author, Story Link, Parent Comment ID.
*   **Audit:** Logs strictly tracked for admin actions and critical user events.
*   **Withdrawal:** Request Amount, Status, User Link.

## 6. Access Control & Security Roles
*   **User:** Standard access to public feeds, own profile, creation of stories/comments/feedback.
*   **Admin:** Full access to all data, moderation tools, and system configurations.
    *   *Dev Note:* Admin access can be simulated in dev via `X-BS-Admin` headers or assigned via Role in Prod.

## 7. Future Roadmap Items (Reserved in Code)
*   **Advanced Comment Features:** Editing comments (`PUT`), Flagging system.
*   **Admin Analytics:** Deeper granular stats for story views and user activity.
*   **Withdrawal Eligibility:** Automated checks before allowing withdrawal requests.

## 8. Development & Deployment
*   **Build:** Maven (`mvn clean install`).
*   **Run:** `mvn spring-boot:run`.
*   **Docker:** Full containerization support for App + MongoDB.
*   **Environment:** managed via `.env` (JWT Secrets, Mail Credentials).

# Breakup AI Service Integration & Workflow

This document details the internal workings of the `breakup_ai` service (Breakup AI Story Processor) and how it integrates with the wider `breakup` ecosystem.

## 1. Service Overview
**What is this service?**
`breakup_ai` is a dedicated **Background Processor** built with Spring Boot. It is responsible for all heavy AI operations required to transform a raw user story (audio or text) into a polished, multimedia narrative.

**Why is it separate?**
It keeps the main `breakup_backend` (the user-facing API) lightweight and responsive. While the main backend handles user requests immediately, this service runs asynchronously to process content that takes time (transcription, image generation, etc.).

## 2. Integration Model
The integration between `breakup_backend` and `breakup_ai` is primarily **Database-Driven** (Sharped Database Pattern).

*   **Shared Resource:** Both services connect to the same MongoDB instance and use the `breakup_stories` database.
*   **The Handoff:**
    1.  **User submits story** to `breakup_backend`.
    2.  `breakup_backend` saves the story with status `PROCESSING_PENDING`.
    3.  `breakup_ai` detects this pending status (via scheduler) and picks it up.
    4.  `breakup_ai` processes the story and updates the document directly in MongoDB.
    5.  `breakup_ai` marks status as `PROCESSED`.
    6.  **User views story**: `breakup_backend` serves the now-processed story to the user.

## 3. Detailed Step-by-Step Workflow (How We Do Things)

The core logic resides in `StoryProcessingService`. The process is linear and robust.

### Step 1: Trigger Mechanism
The service starts processing in two ways:
*   **Scheduled Job:** A Cron job runs every **10 minutes** to fetch all stories with status `PROCESSING_PENDING`.
*   **Manual API Trigger:** An endpoint `POST /api/story/{storyId}/process` can forcefully trigger processing for a specific ID.

### Step 2: Audio Transcription (If Audio Exists)
If the story is an "Audio Story" (contains `audioUrl`):
*   **Action:** Audio file is downloaded and sent to **Google Cloud Speech-to-Text**.
*   **Output:** Returns the raw text transcription and confidence score.
*   **Fallback:** If transcription fails, the process halts or logs the error.

### Step 3: Story Rewrite & Enhancement
The raw text (or transcribed text) is often messy.
*   **Action:** Sent to **OpenAI (GPT-3.5-turbo)**.
*   **Goal:** To rewrite the narrative into a coherent, engaging story while maintaining the original meaning.
*   **Output:** `rewrittenText` stored in the document.

### Step 4: Analytical Extraction
We extract metadata to help categorize and tag the story.
*   **Action:** Analyzed by **OpenAI**.
*   **Extracted Data:**
    *   **Emotions:** (e.g., Sadness: 0.8, Hope: 0.3)
    *   **Keywords/Tags:** For search and filtering.
    *   **Themes:** (e.g., "Long Distance", "Betrayal")
    *   **Cultural Elements:** Context-specific markers.

### Step 5: Paragraph Segmentation
To show images alongside text, we need to break the story into chunks.
*   **Action:** The rewritten story is intelligently split into logical paragraphs.
*   **Result:** A list of paragraphs (e.g., Paragraph 1, Paragraph 2...).

### Step 6: Visual Prompt Engineering
For *each* paragraph, we generate a description for an image.
*   **Action:** **GPT-4o** acts as a "Creative Visual Artist". It reads the paragraph and describes a scene in an "animated, emotional style".
*   **Output:** A list of text prompts (e.g., "A lonely figure sitting on a park bench under a streetlamp...").

### Step 7: Text-to-Image Generation (Visuals)
*This step is implied in the architecture for future expansion or current parallel execution.*
*   Currently, the system prepares the *prompts*. The actual per-paragraph image generation is the next logical step in this pipeline.

### Step 8: Thumbnail Generation
A single cover image for the story card.
*   **Action:** The *entire* story context is used to create one master prompt.
*   **Generation:** **DALL-E 3** generates a high-quality, text-free image (1024x1024).
*   **Storage:** The image is uploaded to **Cloudinary**.
*   **Result:** `thumbnailUrl` saved to the story.

### Step 9: Finalization
*   **Status Update:** The story document's `processingStatus` is set to `PROCESSED`.
*   **Persistence:** The updated document (with all new fields: transcription, analysis, prompts, thumbnail) is saved to MongoDB.

## 4. Technology Stack Summary
*   **Core:** Java 21, Spring Boot 3.3.0
*   **Database:** MongoDB (Spring Data)
*   **AI Models:**
    *   OpenAI GPT-3.5 Turbo (Logic/Text)
    *   OpenAI GPT-4o (Creative Prompts)
    *   OpenAI DALL-E 3 (Images)
    *   Google Cloud Speech-to-Text (Audio)
*   **Cloud Storage:** Cloudinary (Images), Google Cloud Storage (Audio source)

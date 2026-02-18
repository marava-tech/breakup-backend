package com.breakupstories.service;

import com.breakupstories.dto.TranscriptionResponse;
import com.breakupstories.exception.TranscriptionException;
import com.breakupstories.model.StoryDataStore;
import com.breakupstories.repository.StoryDataStoreRepository;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.speech.v1.*;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Map;

@Service
public class TranscriptionService {

    private static final Logger log = LoggerFactory.getLogger(TranscriptionService.class);
    private final StoryDataStoreRepository storyRepository;

    // Language mapping for Indian languages
    private static final Map<String, String> LANGUAGE_MAPPING = Map.of(
            "te", "te-IN", // Telugu
            "hi", "hi-IN", // Hindi
            "ta", "ta-IN", // Tamil
            "kn", "kn-IN", // Kannada
            "ml", "ml-IN", // Malayalam
            "en", "en-US" // English
    );

    public TranscriptionService(StoryDataStoreRepository storyRepository) {
        this.storyRepository = storyRepository;
    }

    @Autowired
    private SpeechClient speechClient;

    @Autowired
    private Storage storageClient;

    /**
     * Process transcription for a story
     */
    public void processTranscription(StoryDataStore story) throws IOException {
        try {
            log.info("Processing transcription for story: {}", story.getStoryId());

            // Validate audio URL
            if (story.getAudioUrl() == null || story.getAudioUrl().trim().isEmpty()) {
                throw new TranscriptionException("Audio URL is null or empty");
            }

            // Download audio file from URL
            File audioFile = downloadAudioFromUrl(story.getAudioUrl());

            try {
                // Call Google Cloud Speech-to-Text API
                TranscriptionResponse transcriptionResponse = transcribeAudio(audioFile, story.getLanguage());

                // Validate transcription response
                if (transcriptionResponse == null) {
                    throw new TranscriptionException("Transcription response is null");
                }

                if (transcriptionResponse.getTranscription() == null ||
                        transcriptionResponse.getTranscription().trim().isEmpty()) {
                    throw new TranscriptionException("Transcription text is null or empty");
                }

                // Update story with transcription response
                story.setTranscriptionResponse(transcriptionResponse);
                story.setTranscriptionCompletedAt(LocalDateTime.now());
                storyRepository.save(story);

                log.info("Successfully processed transcription for story: {} (transcript length: {} chars)",
                        story.getStoryId(), transcriptionResponse.getTranscription().length());

            } finally {
                // Clean up the temporary audio file
                if (audioFile != null && audioFile.exists()) {
                    audioFile.delete();
                }
            }

        } catch (Exception e) {
            log.error("Error processing transcription for story: {}", story.getStoryId(), e);
            story.setTranscriptionError(e.getMessage());
            storyRepository.save(story);
            throw e;
        }
    }

    /**
     * Download audio file from URL with improved error handling
     */
    private File downloadAudioFromUrl(String audioUrl) throws IOException {
        Path tempFile = null;
        try {
            URL url = new URL(audioUrl);
            tempFile = Files.createTempFile("audio_", ".mp3");

            // Create connection with timeout and user agent
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(30000); // 30 seconds
            connection.setReadTimeout(60000); // 60 seconds
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36");

            // Check response code
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                String errorMsg = "HTTP error: " + responseCode + " - " + connection.getResponseMessage();
                try (java.io.InputStream err = connection.getErrorStream()) {
                    if (err != null) {
                        errorMsg += " | " + new String(err.readAllBytes());
                    }
                }
                throw new TranscriptionException(errorMsg);
            }

            // Download the file
            try (var inputStream = connection.getInputStream()) {
                Files.copy(inputStream, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            // Verify the file was downloaded and has content
            long fileSize = tempFile.toFile().length();
            if (!tempFile.toFile().exists() || fileSize == 0) {
                throw new TranscriptionException(
                        "Downloaded file is empty or doesn't exist. Path: " + tempFile + ", Size: " + fileSize);
            }

            log.info("Downloaded audio file from URL: {} (size: {} bytes)", audioUrl, fileSize);
            return tempFile.toFile();

        } catch (java.nio.file.FileAlreadyExistsException e) {
            log.error("Temp file already exists: {}", tempFile);
            throw new TranscriptionException("Failed to create temporary file: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to download audio file from URL: {} - Error: {} - Type: {}", audioUrl, e.getMessage(),
                    e.getClass().getSimpleName());
            throw new TranscriptionException(
                    "Failed to download audio file from URL: " + audioUrl + " - " + e.getMessage(), e);
        }
    }

    /**
     * Core transcription method
     */
    public TranscriptionResponse transcribeAudio(File audioFile, String language) {
        try {
            log.info("Starting Google Cloud transcription: {}", audioFile.getName());

            // Try different audio formats and encodings
            return tryTranscribeWithDifferentFormats(audioFile, language);

        } catch (Exception e) {
            log.error("Transcription failed: {}", e.getMessage());
            throw new TranscriptionException("Transcription failed: " + e.getMessage(), e);
        }
    }

    /**
     * Try transcription with different audio formats
     */
    private TranscriptionResponse tryTranscribeWithDifferentFormats(File audioFile, String language) {
        // Try different encodings
        RecognitionConfig.AudioEncoding[] encodings = {
                RecognitionConfig.AudioEncoding.MP3,
                RecognitionConfig.AudioEncoding.FLAC,
                RecognitionConfig.AudioEncoding.LINEAR16,
                RecognitionConfig.AudioEncoding.OGG_OPUS
        };

        for (RecognitionConfig.AudioEncoding encoding : encodings) {
            try {
                log.info("Trying transcription with encoding: {}", encoding);
                return transcribeWithEncoding(audioFile, language, encoding);
            } catch (Exception e) {
                log.warn("Failed with encoding {}: {}", encoding, e.getMessage());
                // Continue to next encoding
            }
        }

        throw new TranscriptionException("All audio encoding attempts failed");
    }

    /**
     * Transcribe with specific encoding
     */
    private TranscriptionResponse transcribeWithEncoding(File audioFile, String language,
            RecognitionConfig.AudioEncoding encoding) {
        try {
            // Read audio file
            byte[] content = Files.readAllBytes(audioFile.toPath());

            // Create recognition audio
            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(com.google.protobuf.ByteString.copyFrom(content))
                    .build();

            // Get recognition config with specific encoding
            RecognitionConfig config = getRecognitionConfig(language, encoding);

            // Check duration to choose sync vs async
            long duration = getAudioDuration(audioFile);

            if (duration > 60) { // Use async for long audio
                log.info("Long audio ({}s), using async recognition with encoding {}", duration, encoding);
                return transcribeLongAudioGcs(audioFile, language, encoding);
            } else {
                log.info("Short audio ({}s), using sync recognition with encoding {}", duration, encoding);
                return transcribeShortAudio(audioFile, language, encoding);
            }

        } catch (Exception e) {
            log.error("Transcription failed with encoding {}: {}", encoding, e.getMessage());
            throw new TranscriptionException("Transcription failed with encoding " + encoding + ": " + e.getMessage(),
                    e);
        }
    }

    /**
     * Synchronous transcription for short audio (≤ 1 minute)
     */
    private TranscriptionResponse transcribeShortAudio(File audioFile, String language,
            RecognitionConfig.AudioEncoding encoding) {
        try {
            // Read audio file
            byte[] content = Files.readAllBytes(audioFile.toPath());

            // Create recognition audio
            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(com.google.protobuf.ByteString.copyFrom(content))
                    .build();

            // Get recognition config
            RecognitionConfig config = getRecognitionConfig(language, encoding);

            // Perform transcription
            RecognizeResponse response = speechClient.recognize(config, audio);

            return processResults(response.getResultsList(), language);

        } catch (Exception e) {
            log.error("Sync transcription failed: {}", e.getMessage());
            throw new TranscriptionException("Sync transcription failed: " + e.getMessage(), e);
        }
    }

    /**
     * Asynchronous transcription for long audio (> 1 minute)
     */
    private TranscriptionResponse transcribeLongAudioGcs(File audioFile, String language,
            RecognitionConfig.AudioEncoding encoding) {
        String gcsUri = null;
        try {
            // Upload to GCS
            log.info("Uploading to Google Cloud Storage...");
            gcsUri = uploadToGcs(audioFile);

            // Create recognition audio with GCS URI
            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setUri(gcsUri)
                    .build();

            // Get recognition config
            RecognitionConfig config = getRecognitionConfig(language, encoding);

            // Start long-running operation
            log.info("Starting LongRunningRecognize...");
            OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> operation = speechClient
                    .longRunningRecognizeAsync(config, audio);

            // Wait for completion
            LongRunningRecognizeResponse operationResult = operation.get();
            log.info("LongRunningRecognize completed");

            return processResults(operationResult.getResultsList(), language);

        } catch (Exception e) {
            log.error("Async transcription failed: {}", e.getMessage());
            throw new TranscriptionException("Async transcription failed: " + e.getMessage(), e);
        } finally {
            // Cleanup GCS
            if (gcsUri != null) {
                cleanupGcs(gcsUri);
            }
        }
    }

    /**
     * Create recognition configuration
     */
    private RecognitionConfig getRecognitionConfig(String language, RecognitionConfig.AudioEncoding encoding) {
        // Get target language
        String targetLanguage = LANGUAGE_MAPPING.getOrDefault(language, "en-US");

        RecognitionConfig.Builder configBuilder = RecognitionConfig.newBuilder()
                .setEncoding(encoding)
                .setSampleRateHertz(16000)
                .setLanguageCode(targetLanguage)
                .setEnableAutomaticPunctuation(true)
                .setEnableWordTimeOffsets(true)
                .setEnableWordConfidence(true)
                .setModel("latest_long")
                .setUseEnhanced(true);

        // Add alternative language hints for better detection
        if (language == null) {
            configBuilder.addAlternativeLanguageCodes("hi-IN")
                    .addAlternativeLanguageCodes("te-IN")
                    .addAlternativeLanguageCodes("ta-IN")
                    .addAlternativeLanguageCodes("kn-IN")
                    .addAlternativeLanguageCodes("ml-IN")
                    .addAlternativeLanguageCodes("en-IN");
        }

        return configBuilder.build();
    }

    /**
     * Create recognition configuration with default encoding (for backward
     * compatibility)
     */
    private RecognitionConfig getRecognitionConfig(String language) {
        return getRecognitionConfig(language, RecognitionConfig.AudioEncoding.MP3);
    }

    /**
     * Process recognition results
     */
    private TranscriptionResponse processResults(
            java.util.List<SpeechRecognitionResult> results, String language) {

        StringBuilder transcript = new StringBuilder();
        double confidence = 0.0;
        int resultsCount = 0;

        for (SpeechRecognitionResult result : results) {
            if (result.getAlternativesCount() > 0) {
                SpeechRecognitionAlternative alternative = result.getAlternatives(0);
                transcript.append(alternative.getTranscript()).append(" ");
                confidence += alternative.getConfidence();
                resultsCount++;
            }
        }

        // Calculate average confidence
        if (resultsCount > 0) {
            confidence = confidence / resultsCount;
        }

        // Clean the transcript
        String cleanTranscript = transcript.toString().trim();

        // Check for transcription quality issues
        if (cleanTranscript.isEmpty()) {
            throw new TranscriptionException(
                    "No speech detected. The audio may be too noisy, silent, or contain no recognizable speech.");
        }

        if (confidence < 0.2) {
            log.warn("Low confidence transcription ({}). Audio may be noisy or unclear.", confidence);
        }

        // Determine detected language
        String detectedLanguage = language != null ? language : "unknown";

        log.info("Transcription complete. Language: {}, Confidence: {}", detectedLanguage, confidence);

        return TranscriptionResponse.builder()
                .transcription(cleanTranscript)
                .language(detectedLanguage)
                .confidence(confidence)
                .status("SUCCESS")
                .build();
    }

    /**
     * Convert audio to FLAC format
     */
    private File convertAudioToFlac(File audioFile) throws IOException {
        // Create temporary file
        Path tempFile = Files.createTempFile("audio_", ".flac");

        // For now, we'll just copy the file and hope it's already in a compatible
        // format
        // In production, you should use FFmpeg or another audio conversion library
        Files.copy(audioFile.toPath(), tempFile);

        log.info("Converted {} to FLAC: {}", audioFile.getName(), tempFile);
        return tempFile.toFile();
    }

    /**
     * Get audio duration (simplified implementation)
     */
    private long getAudioDuration(File audioFile) {
        // This is a simplified implementation
        // In production, you might want to use FFmpeg to get actual duration
        return audioFile.length() / 16000; // Rough estimate
    }

    /**
     * Upload audio to Google Cloud Storage
     */
    private String uploadToGcs(File audioFile) throws IOException {
        if (storageClient == null) {
            throw new TranscriptionException("Google Cloud Storage client not initialized.");
        }

        // Generate unique bucket and object names
        String bucketName = "audio-transcription-" + UUID.randomUUID().toString().substring(0, 8);
        String objectName = "audio/" + UUID.randomUUID().toString() + ".flac";

        // Create bucket if it doesn't exist
        try {
            storageClient.create(com.google.cloud.storage.BucketInfo.of(bucketName));
            log.info("Created GCS bucket: {}", bucketName);
        } catch (Exception e) {
            log.info("Using existing GCS bucket: {}", bucketName);
        }

        // Upload the file
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        Blob blob = storageClient.create(blobInfo, Files.readAllBytes(audioFile.toPath()));

        // Generate the GCS URI
        String gcsUri = "gs://" + bucketName + "/" + objectName;
        log.info("Uploaded audio to GCS: {}", gcsUri);

        return gcsUri;
    }

    /**
     * Clean up GCS object
     */
    private void cleanupGcs(String gcsUri) {
        try {
            if (storageClient == null) {
                return;
            }

            // Parse the GCS URI
            if (gcsUri.startsWith("gs://")) {
                String path = gcsUri.substring(5);
                String[] parts = path.split("/", 2);
                if (parts.length == 2) {
                    String bucketName = parts[0];
                    String objectName = parts[1];
                    BlobId blobId = BlobId.of(bucketName, objectName);
                    storageClient.delete(blobId);
                    log.info("Cleaned up GCS object: {}", gcsUri);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to cleanup GCS object {}: {}", gcsUri, e.getMessage());
        }
    }
}

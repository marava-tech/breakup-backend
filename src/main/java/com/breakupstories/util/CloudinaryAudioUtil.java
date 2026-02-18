package com.breakupstories.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for Cloudinary audio operations
 */
@Component
public class CloudinaryAudioUtil {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryAudioUtil.class);

    private final Cloudinary cloudinary;

    public CloudinaryAudioUtil(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Upload audio to Cloudinary
     *
     * @param audioData The audio data as byte array
     * @param fileName The filename for the audio
     * @return The Cloudinary URL of the uploaded audio
     * @throws IOException If upload fails
     */
    public String uploadAudio(byte[] audioData, String fileName) throws IOException {
        try {
            log.info("Uploading audio to Cloudinary: {}", fileName);

            // Generate unique public ID
            String publicId = "breakup/audio/" + UUID.randomUUID().toString();

            // Upload to Cloudinary
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    audioData,
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "resource_type", "video", // Cloudinary treats audio as video
                            "folder", "breakup-stories/audio",
                            "format", "mp3"
                    )
            );

            String audioUrl = (String) uploadResult.get("secure_url");
            log.info("Successfully uploaded audio to Cloudinary: {}", audioUrl);

            return audioUrl;

        } catch (Exception e) {
            log.error("Error uploading audio to Cloudinary: {}", e.getMessage());
            throw new IOException("Failed to upload audio to Cloudinary: " + e.getMessage(), e);
        }
    }

    /**
     * Delete audio from Cloudinary
     *
     * @param publicId The public ID of the audio
     * @return True if deletion was successful
     */
    public boolean deleteAudio(String publicId) {
        try {
            log.info("Deleting audio from Cloudinary: {}", publicId);

            Map<String, Object> deleteResult = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

            String result = (String) deleteResult.get("result");
            boolean success = "ok".equals(result);

            if (success) {
                log.info("Successfully deleted audio: {}", publicId);
            } else {
                log.warn("Failed to delete audio: {}", publicId);
            }

            return success;

        } catch (Exception e) {
            log.error("Error deleting audio from Cloudinary: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get audio URL from public ID
     *
     * @param publicId The public ID of the audio
     * @return The audio URL
     */
    public String getAudioUrl(String publicId) {
        try {
            return cloudinary.url().generate(publicId);
        } catch (Exception e) {
            log.error("Error generating audio URL: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get secure audio URL from public ID
     *
     * @param publicId The public ID of the audio
     * @return The secure audio URL
     */
    public String getSecureAudioUrl(String publicId) {
        try {
            return cloudinary.url().secure(true).generate(publicId);
        } catch (Exception e) {
            log.error("Error generating secure audio URL: {}", e.getMessage());
            return null;
        }
    }
}

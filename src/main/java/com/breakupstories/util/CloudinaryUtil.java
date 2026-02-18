package com.breakupstories.util;

import com.cloudinary.Cloudinary;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for Cloudinary operations
 */
@Component
public class CloudinaryUtil {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryUtil.class);
    private final Cloudinary cloudinary;

    public CloudinaryUtil(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Upload image to Cloudinary
     *
     * @param imageData The image data as byte array
     * @param publicId The public ID for the image
     * @return The Cloudinary URL of the uploaded image
     * @throws IOException If upload fails
     */
    public String uploadImage(byte[] imageData, String publicId) throws IOException {
        try {
            log.info("Uploading image to Cloudinary with public ID: {}", publicId);

            Map<String, Object> uploadParams = new HashMap<>();
            uploadParams.put("public_id", publicId);
            uploadParams.put("resource_type", "image");
            uploadParams.put("folder", "breakup-stories");

            Map<String, Object> result = cloudinary.uploader().upload(imageData, uploadParams);
            String url = (String) result.get("secure_url");

            log.info("Successfully uploaded image to Cloudinary: {}", url);
            return url;

        } catch (Exception e) {
            log.error("Error uploading image to Cloudinary: {}", e.getMessage(), e);
            throw new IOException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
        }
    }

    /**
     * Upload audio to Cloudinary
     *
     * @param audioData The audio data as byte array
     * @param publicId The public ID for the audio
     * @return The Cloudinary URL of the uploaded audio
     * @throws IOException If upload fails
     */
    public String uploadAudio(byte[] audioData, String publicId) throws IOException {
        try {
            log.info("Uploading audio to Cloudinary with public ID: {}", publicId);

            Map<String, Object> uploadParams = new HashMap<>();
            uploadParams.put("public_id", publicId);
            uploadParams.put("resource_type", "video");
            uploadParams.put("folder", "breakup-stories");

            Map<String, Object> result = cloudinary.uploader().upload(audioData, uploadParams);
            String url = (String) result.get("secure_url");

            log.info("Successfully uploaded audio to Cloudinary: {}", url);
            return url;

        } catch (Exception e) {
            log.error("Error uploading audio to Cloudinary: {}", e.getMessage(), e);
            throw new IOException("Failed to upload audio to Cloudinary: " + e.getMessage(), e);
        }
    }

    /**
     * Delete resource from Cloudinary
     *
     * @param publicId The public ID of the resource to delete
     * @throws IOException If deletion fails
     */
    public void deleteResource(String publicId) throws IOException {
        try {
            log.info("Deleting resource from Cloudinary: {}", publicId);

            Map<String, Object> result = cloudinary.uploader().destroy(publicId, new HashMap<>());

            log.info("Successfully deleted resource from Cloudinary: {}", publicId);

        } catch (Exception e) {
            log.error("Error deleting resource from Cloudinary: {}", e.getMessage(), e);
            throw new IOException("Failed to delete resource from Cloudinary: " + e.getMessage(), e);
        }
    }
}

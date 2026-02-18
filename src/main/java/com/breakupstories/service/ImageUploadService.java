package com.breakupstories.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.breakupstories.dto.ImageUploadResponse;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageUploadService {

    private static final Logger log = LoggerFactory.getLogger(ImageUploadService.class);

    private final Cloudinary cloudinary;
    private final PromptConfigurationService promptConfig;

    public ImageUploadService(Cloudinary cloudinary, PromptConfigurationService promptConfig) {
        this.cloudinary = cloudinary;
        this.promptConfig = promptConfig;
    }

    /**
     * Upload image from byte array
     */
    public ImageUploadResponse uploadImageFromBytes(byte[] imageBytes, String fileName) {
        try {
            log.info("Uploading image from byte array: {}", fileName);

            // Generate unique public ID
            String publicId = "breakup-stories/" + UUID.randomUUID().toString();

            // Convert byte array to base64 string for Cloudinary
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // Upload to Cloudinary using base64
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    "data:image/png;base64," + base64Image,
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "resource_type", "image",
                            "folder", "breakup-stories",
                            "quality", "auto:low",
                            "fetch_format", "auto"
                    )
            );

            return buildUploadResponse(uploadResult, promptConfig.getPrompt("image_upload_success"));

        } catch (Exception e) {
            log.error("Error uploading image from byte array: {}", e.getMessage());
            return ImageUploadResponse.builder()
                    .status("FAILED")
                    .error(e.getMessage())
                    .build();
        }
    }

    /**
     * Upload image from base64 string
     */
    public ImageUploadResponse uploadImageFromBase64(String base64Image, String fileName) {
        try {
            log.info("Uploading image from base64: {}", fileName);

            // Remove data URL prefix if present
            String base64Data = base64Image;
            if (base64Image.startsWith("data:image")) {
                base64Data = base64Image.substring(base64Image.indexOf(",") + 1);
            }

            // Decode base64 to byte array
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            // Upload using byte array method
            return uploadImageFromBytes(imageBytes, fileName);

        } catch (Exception e) {
            log.error("Error uploading image from base64: {}", e.getMessage());
            return ImageUploadResponse.builder()
                    .status("FAILED")
                    .error(e.getMessage())
                    .build();
        }
    }

    /**
     * Upload image from DALL-E response (base64)
     */
    public ImageUploadResponse uploadDalleImage(String dalleBase64Image) {
        try {
            log.info("Uploading DALL-E generated image");

            // Generate filename for DALL-E image
            String fileName = "dalle-image-" + UUID.randomUUID().toString() + ".png";

            return uploadImageFromBase64(dalleBase64Image, fileName);

        } catch (Exception e) {
            log.error("Error uploading DALL-E image: {}", e.getMessage());
            return ImageUploadResponse.builder()
                    .status("FAILED")
                    .error(e.getMessage())
                    .build();
        }
    }

    /**
     * Delete image from Cloudinary
     */
    public boolean deleteImage(String publicId) {
        try {
            log.info("Deleting image from Cloudinary: {}", publicId);

            Map<String, Object> deleteResult = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

            String result = (String) deleteResult.get("result");
            boolean success = "ok".equals(result);

            if (success) {
                log.info("Successfully deleted image: {}", publicId);
            } else {
                log.warn("Failed to delete image: {}", publicId);
            }

            return success;

        } catch (Exception e) {
            log.error("Error deleting image from Cloudinary: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Build upload response from Cloudinary result
     */
    private ImageUploadResponse buildUploadResponse(Map<String, Object> uploadResult, String status) {
        // Handle bytes field type casting
        Long bytes = null;
        Object bytesObj = uploadResult.get("bytes");
        if (bytesObj != null) {
            if (bytesObj instanceof Integer) {
                bytes = ((Integer) bytesObj).longValue();
            } else if (bytesObj instanceof Long) {
                bytes = (Long) bytesObj;
            }
        }

        return ImageUploadResponse.builder()
                .publicId((String) uploadResult.get("public_id"))
                .url((String) uploadResult.get("url"))
                .secureUrl((String) uploadResult.get("secure_url"))
                .format((String) uploadResult.get("format"))
                .bytes(bytes)
                .width((Integer) uploadResult.get("width"))
                .height((Integer) uploadResult.get("height"))
                .status(status)
                .build();
    }

    /**
     * Get image URL from public ID
     */
    public String getImageUrl(String publicId) {
        try {
            return cloudinary.url().generate(publicId);
        } catch (Exception e) {
            log.error("Error generating image URL: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get secure image URL from public ID
     */
    public String getSecureImageUrl(String publicId) {
        try {
            return cloudinary.url().secure(true).generate(publicId);
        } catch (Exception e) {
            log.error("Error generating secure image URL: {}", e.getMessage());
            return null;
        }
    }
}

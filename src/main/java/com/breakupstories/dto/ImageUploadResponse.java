package com.breakupstories.dto;

public class ImageUploadResponse {

    private String publicId;
    private String url;
    private String secureUrl;
    private String format;
    private Long bytes;
    private Integer width;
    private Integer height;
    private String status;
    private String error;

    // Default constructor
    public ImageUploadResponse() {}

    // All-args constructor
    public ImageUploadResponse(String publicId, String url, String secureUrl, String format, Long bytes, Integer width, Integer height, String status, String error) {
        this.publicId = publicId;
        this.url = url;
        this.secureUrl = secureUrl;
        this.format = format;
        this.bytes = bytes;
        this.width = width;
        this.height = height;
        this.status = status;
        this.error = error;
    }

    // Manual getters and setters for Lombok compatibility
    public String getPublicId() { return publicId; }
    public void setPublicId(String publicId) { this.publicId = publicId; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getSecureUrl() { return secureUrl; }
    public void setSecureUrl(String secureUrl) { this.secureUrl = secureUrl; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public Long getBytes() { return bytes; }
    public void setBytes(Long bytes) { this.bytes = bytes; }

    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }

    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    // Manual builder method for Lombok compatibility
    public static ImageUploadResponseBuilder builder() {
        return new ImageUploadResponseBuilder();
    }

    public static class ImageUploadResponseBuilder {
        private String publicId;
        private String url;
        private String secureUrl;
        private String format;
        private Long bytes;
        private Integer width;
        private Integer height;
        private String status;
        private String error;

        public ImageUploadResponseBuilder publicId(String publicId) { this.publicId = publicId; return this; }
        public ImageUploadResponseBuilder url(String url) { this.url = url; return this; }
        public ImageUploadResponseBuilder secureUrl(String secureUrl) { this.secureUrl = secureUrl; return this; }
        public ImageUploadResponseBuilder format(String format) { this.format = format; return this; }
        public ImageUploadResponseBuilder bytes(Long bytes) { this.bytes = bytes; return this; }
        public ImageUploadResponseBuilder width(Integer width) { this.width = width; return this; }
        public ImageUploadResponseBuilder height(Integer height) { this.height = height; return this; }
        public ImageUploadResponseBuilder status(String status) { this.status = status; return this; }
        public ImageUploadResponseBuilder error(String error) { this.error = error; return this; }

        public ImageUploadResponse build() {
            ImageUploadResponse response = new ImageUploadResponse();
            response.setPublicId(publicId);
            response.setUrl(url);
            response.setSecureUrl(secureUrl);
            response.setFormat(format);
            response.setBytes(bytes);
            response.setWidth(width);
            response.setHeight(height);
            response.setStatus(status);
            response.setError(error);
            return response;
        }
    }
}

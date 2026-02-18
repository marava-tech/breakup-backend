package com.breakupstories.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// REMOVE LOMBOK IMPORTS AND ANNOTATIONS
public class DallERequest {
    private String model;
    private String prompt;
    private Integer n;
    private String size;
    private String quality;
    @JsonProperty("response_format")
    private String responseFormat;
    private String style;

    // Manual getters and setters for Lombok compatibility
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public Integer getN() { return n; }
    public void setN(Integer n) { this.n = n; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getQuality() { return quality; }
    public void setQuality(String quality) { this.quality = quality; }

    public String getResponseFormat() { return responseFormat; }
    public void setResponseFormat(String responseFormat) { this.responseFormat = responseFormat; }

    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }

    // Manual builder method for Lombok compatibility
    public static DallERequestBuilder builder() {
        return new DallERequestBuilder();
    }

    public static class DallERequestBuilder {
        private String model;
        private String prompt;
        private Integer n;
        private String size;
        private String quality;
        private String responseFormat;
        private String style;

        public DallERequestBuilder model(String model) { this.model = model; return this; }
        public DallERequestBuilder prompt(String prompt) { this.prompt = prompt; return this; }
        public DallERequestBuilder n(Integer n) { this.n = n; return this; }
        public DallERequestBuilder size(String size) { this.size = size; return this; }
        public DallERequestBuilder quality(String quality) { this.quality = quality; return this; }
        public DallERequestBuilder responseFormat(String responseFormat) { this.responseFormat = responseFormat; return this; }
        public DallERequestBuilder style(String style) { this.style = style; return this; }

        public DallERequest build() {
            DallERequest request = new DallERequest();
            request.setModel(model);
            request.setPrompt(prompt);
            request.setN(n);
            request.setSize(size);
            request.setQuality(quality);
            request.setResponseFormat(responseFormat);
            request.setStyle(style);
            return request;
        }
    }
}

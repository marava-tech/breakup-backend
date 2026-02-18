package com.breakupstories.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO for DALL-E API response
 */
public class DallEResponse {

    private long created;
    private List<Data> data;

    // Manual getters and setters for Lombok compatibility
    public long getCreated() { return created; }
    public void setCreated(long created) { this.created = created; }

    public List<Data> getData() { return data; }
    public void setData(List<Data> data) { this.data = data; }

    // Manual builder method for Lombok compatibility
    public static DallEResponseBuilder builder() {
        return new DallEResponseBuilder();
    }

    public static class DallEResponseBuilder {
        private long created;
        private List<Data> data;

        public DallEResponseBuilder created(long created) { this.created = created; return this; }
        public DallEResponseBuilder data(List<Data> data) { this.data = data; return this; }

        public DallEResponse build() {
            DallEResponse response = new DallEResponse();
            response.setCreated(created);
            response.setData(data);
            return response;
        }
    }

    public static class Data {
        private String url;
        @JsonProperty("revised_prompt")
        private String revisedPrompt;

        // Manual getters and setters for Lombok compatibility
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public String getRevisedPrompt() { return revisedPrompt; }
        public void setRevisedPrompt(String revisedPrompt) { this.revisedPrompt = revisedPrompt; }

        // Manual builder method for Lombok compatibility
        public static DataBuilder builder() {
            return new DataBuilder();
        }

        public static class DataBuilder {
            private String url;
            private String revisedPrompt;

            public DataBuilder url(String url) { this.url = url; return this; }
            public DataBuilder revisedPrompt(String revisedPrompt) { this.revisedPrompt = revisedPrompt; return this; }

            public Data build() {
                Data data = new Data();
                data.setUrl(url);
                data.setRevisedPrompt(revisedPrompt);
                return data;
            }
        }
    }
}

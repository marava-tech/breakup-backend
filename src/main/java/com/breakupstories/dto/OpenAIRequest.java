package com.breakupstories.dto;

import java.util.List;

public class OpenAIRequest {
    private String model;
    private List<Message> messages;
    private Integer max_tokens;
    private Double temperature;

    // Manual getters and setters for Lombok compatibility
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }

    public Integer getMax_tokens() { return max_tokens; }
    public void setMax_tokens(Integer max_tokens) { this.max_tokens = max_tokens; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    // Manual builder method for Lombok compatibility
    public static OpenAIRequestBuilder builder() {
        return new OpenAIRequestBuilder();
    }

    public static class OpenAIRequestBuilder {
        private String model;
        private List<Message> messages;
        private Integer max_tokens;
        private Double temperature;

        public OpenAIRequestBuilder model(String model) { this.model = model; return this; }
        public OpenAIRequestBuilder messages(List<Message> messages) { this.messages = messages; return this; }
        public OpenAIRequestBuilder maxTokens(Integer max_tokens) { this.max_tokens = max_tokens; return this; }
        public OpenAIRequestBuilder temperature(Double temperature) { this.temperature = temperature; return this; }

        public OpenAIRequest build() {
            OpenAIRequest request = new OpenAIRequest();
            request.setModel(model);
            request.setMessages(messages);
            request.setMax_tokens(max_tokens);
            request.setTemperature(temperature);
            return request;
        }
    }

    public static class Message {
        private String role;
        private String content;

        // Manual getters and setters for Lombok compatibility
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        // Manual builder method for Lombok compatibility
        public static MessageBuilder builder() {
            return new MessageBuilder();
        }

        public static class MessageBuilder {
            private String role;
            private String content;

            public MessageBuilder role(String role) { this.role = role; return this; }
            public MessageBuilder content(String content) { this.content = content; return this; }

            public Message build() {
                Message message = new Message();
                message.setRole(role);
                message.setContent(content);
                return message;
            }
        }
    }
}

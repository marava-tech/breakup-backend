package com.breakupstories.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO for OpenAI API response
 */
public class OpenAIResponse {

    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;

    // Manual getters and setters for Lombok compatibility
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getObject() { return object; }
    public void setObject(String object) { this.object = object; }

    public long getCreated() { return created; }
    public void setCreated(long created) { this.created = created; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public List<Choice> getChoices() { return choices; }
    public void setChoices(List<Choice> choices) { this.choices = choices; }

    public Usage getUsage() { return usage; }
    public void setUsage(Usage usage) { this.usage = usage; }

    // Manual builder method for Lombok compatibility
    public static OpenAIResponseBuilder builder() {
        return new OpenAIResponseBuilder();
    }

    public static class OpenAIResponseBuilder {
        private String id;
        private String object;
        private long created;
        private String model;
        private List<Choice> choices;
        private Usage usage;

        public OpenAIResponseBuilder id(String id) { this.id = id; return this; }
        public OpenAIResponseBuilder object(String object) { this.object = object; return this; }
        public OpenAIResponseBuilder created(long created) { this.created = created; return this; }
        public OpenAIResponseBuilder model(String model) { this.model = model; return this; }
        public OpenAIResponseBuilder choices(List<Choice> choices) { this.choices = choices; return this; }
        public OpenAIResponseBuilder usage(Usage usage) { this.usage = usage; return this; }

        public OpenAIResponse build() {
            OpenAIResponse response = new OpenAIResponse();
            response.setId(id);
            response.setObject(object);
            response.setCreated(created);
            response.setModel(model);
            response.setChoices(choices);
            response.setUsage(usage);
            return response;
        }
    }

    public static class Choice {
        private int index;
        private Message message;
        private String finishReason;

        // Manual getters and setters for Lombok compatibility
        public int getIndex() { return index; }
        public void setIndex(int index) { this.index = index; }

        public Message getMessage() { return message; }
        public void setMessage(Message message) { this.message = message; }

        public String getFinishReason() { return finishReason; }
        public void setFinishReason(String finishReason) { this.finishReason = finishReason; }

        // Manual builder method for Lombok compatibility
        public static ChoiceBuilder builder() {
            return new ChoiceBuilder();
        }

        public static class ChoiceBuilder {
            private int index;
            private Message message;
            private String finishReason;

            public ChoiceBuilder index(int index) { this.index = index; return this; }
            public ChoiceBuilder message(Message message) { this.message = message; return this; }
            public ChoiceBuilder finishReason(String finishReason) { this.finishReason = finishReason; return this; }

            public Choice build() {
                Choice choice = new Choice();
                choice.setIndex(index);
                choice.setMessage(message);
                choice.setFinishReason(finishReason);
                return choice;
            }
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

    public static class Usage {
        @JsonProperty("prompt_tokens")
        private int promptTokens;

        @JsonProperty("completion_tokens")
        private int completionTokens;

        @JsonProperty("total_tokens")
        private int totalTokens;

        // Manual getters and setters for Lombok compatibility
        public int getPromptTokens() { return promptTokens; }
        public void setPromptTokens(int promptTokens) { this.promptTokens = promptTokens; }

        public int getCompletionTokens() { return completionTokens; }
        public void setCompletionTokens(int completionTokens) { this.completionTokens = completionTokens; }

        public int getTotalTokens() { return totalTokens; }
        public void setTotalTokens(int totalTokens) { this.totalTokens = totalTokens; }

        // Manual builder method for Lombok compatibility
        public static UsageBuilder builder() {
            return new UsageBuilder();
        }

        public static class UsageBuilder {
            private int promptTokens;
            private int completionTokens;
            private int totalTokens;

            public UsageBuilder promptTokens(int promptTokens) { this.promptTokens = promptTokens; return this; }
            public UsageBuilder completionTokens(int completionTokens) { this.completionTokens = completionTokens; return this; }
            public UsageBuilder totalTokens(int totalTokens) { this.totalTokens = totalTokens; return this; }

            public Usage build() {
                Usage usage = new Usage();
                usage.setPromptTokens(promptTokens);
                usage.setCompletionTokens(completionTokens);
                usage.setTotalTokens(totalTokens);
                return usage;
            }
        }
    }
}

package com.breakupstories.dto;

/**
 * Response DTO for consoling message generation
 */
public class ConsolingMessageResponse {

    /**
     * The story ID
     */
    private String storyId;

    /**
     * The user ID
     */
    private String userId;

    /**
     * The persona used for generating the message
     */
    private String persona;

    /**
     * User details
     */
    private UserDetails user;

    /**
     * The generated consoling message
     */
    private String consolingMessage;

    // Manual getters and setters for Lombok compatibility
    public String getStoryId() { return storyId; }
    public void setStoryId(String storyId) { this.storyId = storyId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPersona() { return persona; }
    public void setPersona(String persona) { this.persona = persona; }

    public UserDetails getUser() { return user; }
    public void setUser(UserDetails user) { this.user = user; }

    public String getConsolingMessage() { return consolingMessage; }
    public void setConsolingMessage(String consolingMessage) { this.consolingMessage = consolingMessage; }

    // Manual builder method for Lombok compatibility
    public static ConsolingMessageResponseBuilder builder() {
        return new ConsolingMessageResponseBuilder();
    }

    public static class ConsolingMessageResponseBuilder {
        private String storyId;
        private String userId;
        private String persona;
        private UserDetails user;
        private String consolingMessage;

        public ConsolingMessageResponseBuilder storyId(String storyId) { this.storyId = storyId; return this; }
        public ConsolingMessageResponseBuilder userId(String userId) { this.userId = userId; return this; }
        public ConsolingMessageResponseBuilder persona(String persona) { this.persona = persona; return this; }
        public ConsolingMessageResponseBuilder user(UserDetails user) { this.user = user; return this; }
        public ConsolingMessageResponseBuilder consolingMessage(String consolingMessage) { this.consolingMessage = consolingMessage; return this; }

        public ConsolingMessageResponse build() {
            ConsolingMessageResponse response = new ConsolingMessageResponse();
            response.setStoryId(storyId);
            response.setUserId(userId);
            response.setPersona(persona);
            response.setUser(user);
            response.setConsolingMessage(consolingMessage);
            return response;
        }
    }

    /**
     * User details DTO
     */
    public static class UserDetails {
        private String name;
        private Integer age;
        private String gender;
        private String preferredStoryLanguage;

        // Manual getters and setters for Lombok compatibility
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }

        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }

        public String getPreferredStoryLanguage() { return preferredStoryLanguage; }
        public void setPreferredStoryLanguage(String preferredStoryLanguage) { this.preferredStoryLanguage = preferredStoryLanguage; }

        // Manual builder method for Lombok compatibility
        public static UserDetailsBuilder builder() {
            return new UserDetailsBuilder();
        }

        public static class UserDetailsBuilder {
            private String name;
            private Integer age;
            private String gender;
            private String preferredStoryLanguage;

            public UserDetailsBuilder name(String name) { this.name = name; return this; }
            public UserDetailsBuilder age(Integer age) { this.age = age; return this; }
            public UserDetailsBuilder gender(String gender) { this.gender = gender; return this; }
            public UserDetailsBuilder preferredStoryLanguage(String preferredStoryLanguage) { this.preferredStoryLanguage = preferredStoryLanguage; return this; }

            public UserDetails build() {
                UserDetails userDetails = new UserDetails();
                userDetails.setName(name);
                userDetails.setAge(age);
                userDetails.setGender(gender);
                userDetails.setPreferredStoryLanguage(preferredStoryLanguage);
                return userDetails;
            }
        }
    }
}

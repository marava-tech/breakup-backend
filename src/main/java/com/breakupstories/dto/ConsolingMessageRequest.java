package com.breakupstories.dto;

/**
 * Request DTO for consoling message generation
 */
public class ConsolingMessageRequest {

    /**
     * The story ID to generate consoling message for
     */
    private String storyId;

    /**
     * The persona to use for generating the consoling message
     * Valid values: MALE_FRIEND, FEMALE_FRIEND, FATHER, MOTHER
     */
    private String persona;
}

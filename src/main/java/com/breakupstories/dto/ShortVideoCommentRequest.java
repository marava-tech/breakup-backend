package com.breakupstories.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class ShortVideoCommentRequest {
    @NotBlank(message = "Comment text cannot be empty")
    @Size(max = 1000, message = "Comment must be less than 1000 characters")
    private String text;

    // Optional, if it's a reply
    private String parentId;
}

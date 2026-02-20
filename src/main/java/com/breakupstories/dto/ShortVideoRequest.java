package com.breakupstories.dto;

import com.breakupstories.model.ShortVideo.VideoStatus;
import lombok.Data;

import java.util.List;

@Data
public class ShortVideoRequest {
    private String title;
    private String description;
    private String videoUrl;
    private String thumbnailUrl;
    private String language;
    private List<String> tags;
    private VideoStatus status;
}

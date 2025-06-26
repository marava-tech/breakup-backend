package com.breakupstories.service;

import com.breakupstories.dto.LikeRequest;
import com.breakupstories.dto.LikeResponse;
import com.breakupstories.dto.PagedResponse;
import com.breakupstories.dto.StoryResponse;
import com.breakupstories.dto.CommentRequest;
import com.breakupstories.dto.CommentResponse;
import com.breakupstories.enums.LANGUAGE;
import com.breakupstories.model.Content;
import com.breakupstories.model.Emotion;
import com.breakupstories.model.Keyword;
import com.breakupstories.model.Story;
import com.breakupstories.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoryService {
    
    private final StoryRepository storyRepository;
    private final UploadService uploadService;
    private final LikeService likeService;
    private final CommentService commentService;

    public StoryResponse createStory(String userId, MultipartHttpServletRequest request) {
        log.info("Creating story for user: {}", userId);
        
        try {
            // Step 1: Upload the audio to upload service and get the URL
            MultipartFile audioFile = request.getFile("audio");
            if (audioFile == null || audioFile.isEmpty()) {
                throw new IllegalArgumentException("Audio file is required");
            }
            
            log.info("Uploading audio file: {} ({} bytes)", audioFile.getOriginalFilename(), audioFile.getSize());
            var uploadResponse = uploadService.uploadFile(audioFile);
            String audioUrl = uploadResponse.getData().get(0);
            log.info("Audio uploaded successfully: {}", audioUrl);
            
            // Step 2: Create initial story with PROCESSING status
            Story story = Story.builder()
                    .userId(userId)
                    .title("Processing...") // Will be updated after AI processing
                    .audioUrl(audioUrl)
                    .shareLink("") // Will be generated after processing
                    .viewCount(0L)
                    .status(Story.StoryStatus.PROCESSING)
                    .contents(new ArrayList<>()) // Will be populated after AI processing
                    .tags(new ArrayList<>()) // Will be populated after AI processing
                    .emotions(new ArrayList<>()) // Will be populated after AI processing
                    .keywords(new ArrayList<>()) // Will be populated after AI processing
                    .build();
            
            Story savedStory = storyRepository.save(story);
            log.info("Initial story created with ID: {}", savedStory.getId());
            
            // Step 3: Start async AI processing
            processStoryWithAIAsync(savedStory.getId());
            
            return StoryResponse.fromStory(savedStory);
            
        } catch (Exception e) {
            log.error("Error creating story for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to create story: " + e.getMessage(), e);
        }
    }
    
    /**
     * Async AI processing workflow
     * This simulates the AI service calls that will be integrated later
     */
    @Async
    public CompletableFuture<Void> processStoryWithAIAsync(String storyId) {
        log.info("Starting async AI processing for story: {}", storyId);
        
        try {
            // Simulate processing time (10 seconds)
            Thread.sleep(10000);
            
            // Step 1: Mock Transcription
            String transcription = mockTranscription();
            log.info("Transcription completed for story: {}", storyId);

            // Step 1.5: Mock Audio Language Detection
            LANGUAGE language = mockDetectAudioLanguage(transcription);
            log.info("Audio language detected for story {}: {}", storyId, language);
            
            // Step 2: Mock Detailed Story Creation
            String detailedStory = mockCreateDetailedStory(transcription);
            log.info("Detailed story created for story: {}", storyId);
            
            // Step 3: Mock Title Generation
            String title = mockCreateTitle(detailedStory);
            log.info("Title generated for story: {}", storyId);
            
            // Step 4: Mock Animated Images (skip for now as mentioned in comments)
            List<String> animatedImages = mockGenerateAnimatedImages(detailedStory);
            log.info("Animated images generated for story: {}", storyId);
            
            // Step 5: Mock Content Creation with Images
            List<Content> contents = mockCreateContents(detailedStory, animatedImages);
            log.info("Contents created for story: {}", storyId);
            
            // Step 6: Mock Emotions Analysis
            List<Emotion> emotions = mockGetEmotions(detailedStory);
            log.info("Emotions analyzed for story: {}", storyId);
            
            // Step 7: Mock Tags Generation
            List<String> tags = mockGetTags(detailedStory);
            log.info("Tags generated for story: {}", storyId);
            
            // Step 8: Mock Keywords Extraction
            List<Keyword> keywords = mockGetKeywords(detailedStory);
            log.info("Keywords extracted for story: {}", storyId);
            
            // Step 9: Mock Shareable Link Generation
            String shareLink = mockCreateShareableLink(storyId);
            log.info("Shareable link created for story: {}", storyId);
            
            // Update the story with all processed data including language
            updateStoryWithAIResults(storyId, title, contents, tags, emotions, keywords, shareLink, language);
            
            log.info("AI processing completed successfully for story: {}", storyId);
            
        } catch (Exception e) {
            log.error("Error in AI processing for story {}: {}", storyId, e.getMessage(), e);
            // Update story status to REJECTED (failed processing)
            updateStoryStatus(storyId, Story.StoryStatus.REJECTED);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Mock AI processing workflow (synchronous version - kept for backward compatibility)
     * This simulates the AI service calls that will be integrated later
     */
    private void processStoryWithAI(String storyId) {
        log.info("Starting AI processing for story: {}", storyId);
        
        try {
            // Simulate processing time (10 seconds)
            Thread.sleep(10000);
            
            // Step 1: Mock Transcription
            String transcription = mockTranscription();
            log.info("Transcription completed for story: {}", storyId);

            // Step 1.5: Mock Audio Language Detection
            LANGUAGE language = mockDetectAudioLanguage(transcription);
            log.info("Audio language detected for story {}: {}", storyId, language);
            
            // Step 2: Mock Detailed Story Creation
            String detailedStory = mockCreateDetailedStory(transcription);
            log.info("Detailed story created for story: {}", storyId);
            
            // Step 3: Mock Title Generation
            String title = mockCreateTitle(detailedStory);
            log.info("Title generated for story: {}", storyId);
            
            // Step 4: Mock Animated Images (skip for now as mentioned in comments)
            List<String> animatedImages = mockGenerateAnimatedImages(detailedStory);
            log.info("Animated images generated for story: {}", storyId);
            
            // Step 5: Mock Content Creation with Images
            List<Content> contents = mockCreateContents(detailedStory, animatedImages);
            log.info("Contents created for story: {}", storyId);
            
            // Step 6: Mock Emotions Analysis
            List<Emotion> emotions = mockGetEmotions(detailedStory);
            log.info("Emotions analyzed for story: {}", storyId);
            
            // Step 7: Mock Tags Generation
            List<String> tags = mockGetTags(detailedStory);
            log.info("Tags generated for story: {}", storyId);
            
            // Step 8: Mock Keywords Extraction
            List<Keyword> keywords = mockGetKeywords(detailedStory);
            log.info("Keywords extracted for story: {}", storyId);
            
            // Step 9: Mock Shareable Link Generation
            String shareLink = mockCreateShareableLink(storyId);
            log.info("Shareable link created for story: {}", storyId);
            
            // Update the story with all processed data including language
            updateStoryWithAIResults(storyId, title, contents, tags, emotions, keywords, shareLink, language);
            
            log.info("AI processing completed successfully for story: {}", storyId);
            
        } catch (Exception e) {
            log.error("Error in AI processing for story {}: {}", storyId, e.getMessage(), e);
            // Update story status to REJECTED (failed processing)
            updateStoryStatus(storyId, Story.StoryStatus.REJECTED);
            throw new RuntimeException("AI processing failed: " + e.getMessage(), e);
        }
    }
    
    // Mock AI Service Methods
    
    /**
     * Mock audio language detection
     * In real implementation, this would call an AI service to detect the language
     * @param transcription The transcribed audio text
     * @return Detected language
     */
    private LANGUAGE mockDetectAudioLanguage(String transcription) {
        // Mock language detection based on some keywords or patterns
        String text = transcription.toLowerCase();
        
        // Simple keyword-based detection (in real implementation, use proper NLP/AI)
        if (text.contains("నేను") || text.contains("మీరు") || text.contains("అతను") || text.contains("ఆమె")) {
            return LANGUAGE.TELUGU;
        } else if (text.contains("मैं") || text.contains("आप") || text.contains("वह") || text.contains("यह")) {
            return LANGUAGE.HINDI;
        } else if (text.contains("நான்") || text.contains("நீங்கள்") || text.contains("அவன்") || text.contains("அவள்")) {
            return LANGUAGE.TAMIL;
        } else if (text.contains("ನಾನು") || text.contains("ನೀವು") || text.contains("ಅವನು") || text.contains("ಅವಳು")) {
            return LANGUAGE.KANNADA;
        } else if (text.contains("ഞാൻ") || text.contains("നിങ്ങൾ") || text.contains("അവൻ") || text.contains("അവൾ")) {
            return LANGUAGE.MALAYALAM;
        } else if (text.contains("আমি") || text.contains("আপনি") || text.contains("সে") || text.contains("এটা")) {
            return LANGUAGE.BENGALI;
        } else if (text.contains("मी") || text.contains("तुम्ही") || text.contains("तो") || text.contains("हे")) {
            return LANGUAGE.MARATHI;
        } else if (text.contains("હું") || text.contains("તમે") || text.contains("તે") || text.contains("આ")) {
            return LANGUAGE.GUJARATI;
        } else if (text.contains("ਮੈਂ") || text.contains("ਤੁਸੀਂ") || text.contains("ਉਹ") || text.contains("ਇਹ")) {
            return LANGUAGE.PUNJABI;
        } else if (text.contains("میں") || text.contains("آپ") || text.contains("وہ") || text.contains("یہ")) {
            return LANGUAGE.URDU;
        } else {
            // Default to English if no specific language patterns detected
            return LANGUAGE.ENGLISH;
        }
    }
    
    private String mockTranscription() {
        return "I remember the day we first met. It was raining, and she was standing under that old oak tree, " +
               "looking so beautiful with her hair slightly wet. We talked for hours, and I knew right then " +
               "that she was the one. But life has a way of testing us, and sometimes love isn't enough to " +
               "overcome the challenges we face. We grew apart, and now I'm left with memories and a heart " +
               "that still beats for someone who's no longer mine.";
    }
    
    private String mockCreateDetailedStory(String transcription) {
        return "In the quiet corners of my mind, I still hear the echo of her laughter. " +
               "It was a Tuesday afternoon when the rain decided to play matchmaker, bringing two souls " +
               "together under the shelter of an ancient oak tree. She was reading a book, completely " +
               "unaware that her life was about to change forever.\n\n" +
               "Our eyes met, and in that moment, time stood still. The world around us faded into " +
               "nothingness as we discovered the magic of connection. We talked about everything and nothing, " +
               "sharing dreams, fears, and the kind of intimate thoughts that only lovers share.\n\n" +
               "But love, as beautiful as it is, can be fragile. Life threw us curveballs, and we found " +
               "ourselves drifting apart like ships in the night. The distance grew, not just physical, " +
               "but emotional too. We became strangers who once knew each other's hearts.\n\n" +
               "Now, as I sit here writing this story, I realize that some loves are meant to be memories " +
               "rather than forever. And that's okay. Because even though she's no longer mine, the love " +
               "we shared will always be a part of who I am.";
    }
    
    private String mockCreateTitle(String detailedStory) {
        String[] titles = {
            "Love Under the Oak Tree",
            "When Rain Brought Us Together",
            "Memories of a Tuesday Afternoon",
            "The Story of Us",
            "Fragile Hearts, Beautiful Memories",
            "Ships in the Night",
            "A Love That Became Memory",
            "The Echo of Her Laughter"
        };
        return titles[(int) (Math.random() * titles.length)];
    }
    
    private List<String> mockGenerateAnimatedImages(String detailedStory) {
        // Mock animated image URLs (skip for now as mentioned in comments)
        return Arrays.asList(
            "https://res.cloudinary.com/dohsebpd1/image/upload/v1750951801/animated_rain_scene.gif",
            "https://res.cloudinary.com/dohsebpd1/image/upload/v1750951801/animated_oak_tree.gif",
            "https://res.cloudinary.com/dohsebpd1/image/upload/v1750951801/animated_hearts.gif",
            "https://res.cloudinary.com/dohsebpd1/image/upload/v1750951801/animated_memories.gif"
        );
    }
    
    private List<Content> mockCreateContents(String detailedStory, List<String> animatedImages) {
        List<Content> contents = new ArrayList<>();
        
        // Split the story into paragraphs
        String[] paragraphs = detailedStory.split("\n\n");
        
        int imageIndex = 0;
        for (int i = 0; i < paragraphs.length; i++) {
            // Add text content
            Content textContent = Content.builder()
                    .type(Content.ContentType.TEXT)
                    .data(paragraphs[i].trim())
                    .orderIndex(i * 2)
                    .build();
            contents.add(textContent);
            
            // Add image content after every other paragraph (if images available)
            if (imageIndex < animatedImages.size() && i > 0) {
                Content imageContent = Content.builder()
                        .type(Content.ContentType.IMAGE)
                        .data(animatedImages.get(imageIndex))
                        .orderIndex(i * 2 + 1)
                        .build();
                contents.add(imageContent);
                imageIndex++;
            }
        }
        
        return contents;
    }
    
    private List<Emotion> mockGetEmotions(String detailedStory) {
        return Arrays.asList(
            Emotion.builder()
                    .type(Emotion.EmotionType.SAD)
                    .score(0.85)
                    .build(),
            Emotion.builder()
                    .type(Emotion.EmotionType.CALM)
                    .score(0.72)
                    .build(),
            Emotion.builder()
                    .type(Emotion.EmotionType.HAPPY)
                    .score(0.68)
                    .build(),
            Emotion.builder()
                    .type(Emotion.EmotionType.EXCITED)
                    .score(0.45)
                    .build(),
            Emotion.builder()
                    .type(Emotion.EmotionType.SURPRISED)
                    .score(0.38)
                    .build()
        );
    }
    
    private List<String> mockGetTags(String detailedStory) {
        return Arrays.asList(
            "breakup",
            "love",
            "memories",
            "rain",
            "oak tree",
            "nostalgia",
            "healing",
            "moving on"
        );
    }
    
    private List<Keyword> mockGetKeywords(String detailedStory) {
        return Arrays.asList(
            Keyword.builder()
                    .key("name")
                    .value("Madhu")
                    .build(),
            Keyword.builder()
                    .key("location")
                    .value("Hyderabad")
                    .build(),
            Keyword.builder()
                    .key("place")
                    .value("Oak Tree")
                    .build(),
            Keyword.builder()
                    .key("weather")
                    .value("Rain")
                    .build(),
            Keyword.builder()
                    .key("emotion")
                    .value("Love")
                    .build(),
            Keyword.builder()
                    .key("relationship_status")
                    .value("Breakup")
                    .build(),
            Keyword.builder()
                    .key("time_period")
                    .value("Tuesday Afternoon")
                    .build(),
            Keyword.builder()
                    .key("memory")
                    .value("First Meeting")
                    .build()
        );
    }
    
    private String mockCreateShareableLink(String storyId) {
        return "https://breakupstories.app/story/" + storyId;
    }
    
    private void updateStoryWithAIResults(String storyId, String title, List<Content> contents, 
                                        List<String> tags, List<Emotion> emotions, 
                                        List<Keyword> keywords, String shareLink, LANGUAGE language) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found: " + storyId));
        
        story.setTitle(title);
        story.setContents(contents);
        story.setTags(tags);
        story.setEmotions(emotions);
        story.setKeywords(keywords);
        story.setShareLink(shareLink);
        story.setStatus(Story.StoryStatus.ACTIVE);
        story.setAudioLanguage(language);
        
        storyRepository.save(story);
        log.info("Story updated with AI results: {}", storyId);
    }
    
    private void updateStoryStatus(String storyId, Story.StoryStatus status) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found: " + storyId));
        
        story.setStatus(status);
        storyRepository.save(story);
        log.info("Story status updated to {}: {}", status, storyId);
    }
    
    public PagedResponse<StoryResponse> getStories(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Story> storyPage = storyRepository.findByStatus(Story.StoryStatus.ACTIVE, pageable);
        
        List<StoryResponse> stories = storyPage.getContent().stream()
                .map(story -> {
                    long likeCount = getLikeCount(story.getId());
                    long commentCount = getCommentCount(story.getId());
                    return StoryResponse.fromStory(story, false, likeCount, commentCount);
                })
                .collect(Collectors.toList());
        
        return PagedResponse.of(stories, page, size, storyPage.getTotalElements());
    }
    
    /**
     * Get trending stories sorted by view count
     * @param page Page number
     * @param size Page size
     * @return PagedResponse of trending stories
     */
    public PagedResponse<StoryResponse> getTrendingStories(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Story> storyPage = storyRepository.findByStatusOrderByViewCountDesc(Story.StoryStatus.ACTIVE, pageable);
        
        List<StoryResponse> stories = storyPage.getContent().stream()
                .map(story -> {
                    long likeCount = getLikeCount(story.getId());
                    long commentCount = getCommentCount(story.getId());
                    return StoryResponse.fromStory(story, false, likeCount, commentCount);
                })
                .collect(Collectors.toList());
        
        return PagedResponse.of(stories, page, size, storyPage.getTotalElements());
    }
    
    /**
     * Get trending stories with user context (includes likedByMe status)
     * @param currentUserId The current user ID
     * @param page Page number
     * @param size Page size
     * @return PagedResponse of trending stories with user context
     */
    public PagedResponse<StoryResponse> getTrendingStories(String currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Story> storyPage = storyRepository.findByStatusOrderByViewCountDesc(Story.StoryStatus.ACTIVE, pageable);
        
        List<StoryResponse> stories = storyPage.getContent().stream()
                .map(story -> {
                    boolean likedByMe = likeService.isLiked(currentUserId, story.getId());
                    long likeCount = getLikeCount(story.getId());
                    long commentCount = getCommentCount(story.getId());
                    return StoryResponse.fromStory(story, likedByMe, likeCount, commentCount);
                })
                .collect(Collectors.toList());
        
        return PagedResponse.of(stories, page, size, storyPage.getTotalElements());
    }
    
    /**
     * Get stories with user context (includes likedByMe status)
     * @param currentUserId The current user ID
     * @param page Page number
     * @param size Page size
     * @return PagedResponse of stories with user context
     */
    public PagedResponse<StoryResponse> getStories(String currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Story> storyPage = storyRepository.findByStatus(Story.StoryStatus.ACTIVE, pageable);
        
        List<StoryResponse> stories = storyPage.getContent().stream()
                .map(story -> {
                    boolean likedByMe = likeService.isLiked(currentUserId, story.getId());
                    long likeCount = getLikeCount(story.getId());
                    long commentCount = getCommentCount(story.getId());
                    return StoryResponse.fromStory(story, likedByMe, likeCount, commentCount);
                })
                .collect(Collectors.toList());
        
        return PagedResponse.of(stories, page, size, storyPage.getTotalElements());
    }
    
    /**
     * Get stories by language
     * @param language The language to filter by
     * @param page Page number
     * @param size Page size
     * @return PagedResponse of stories in the specified language
     */
    public PagedResponse<StoryResponse> getStoriesByLanguage(LANGUAGE language, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Story> storyPage = storyRepository.findByAudioLanguageAndStatus(language, Story.StoryStatus.ACTIVE, pageable);
        
        List<StoryResponse> stories = storyPage.getContent().stream()
                .map(story -> {
                    long likeCount = getLikeCount(story.getId());
                    long commentCount = getCommentCount(story.getId());
                    return StoryResponse.fromStory(story, false, likeCount, commentCount);
                })
                .collect(Collectors.toList());
        
        return PagedResponse.of(stories, page, size, storyPage.getTotalElements());
    }
    
    /**
     * Get stories by language with user context (includes likedByMe status)
     * @param language The language to filter by
     * @param currentUserId The current user ID
     * @param page Page number
     * @param size Page size
     * @return PagedResponse of stories in the specified language with user context
     */
    public PagedResponse<StoryResponse> getStoriesByLanguage(LANGUAGE language, String currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Story> storyPage = storyRepository.findByAudioLanguageAndStatus(language, Story.StoryStatus.ACTIVE, pageable);
        
        List<StoryResponse> stories = storyPage.getContent().stream()
                .map(story -> {
                    boolean likedByMe = likeService.isLiked(currentUserId, story.getId());
                    long likeCount = getLikeCount(story.getId());
                    long commentCount = getCommentCount(story.getId());
                    return StoryResponse.fromStory(story, likedByMe, likeCount, commentCount);
                })
                .collect(Collectors.toList());
        
        return PagedResponse.of(stories, page, size, storyPage.getTotalElements());
    }
    
    public StoryResponse getStoryById(String storyId, String currentUserId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found with ID: " + storyId));
        
        // Check if the current user liked this story
        boolean likedByMe = likeService.isLiked(currentUserId, storyId);
        
        long likeCount = getLikeCount(storyId);
        long commentCount = getCommentCount(storyId);
        
        return StoryResponse.fromStory(story, likedByMe, likeCount, commentCount);
    }
    
    public StoryResponse getStoryById(String storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found with ID: " + storyId));
        
        long likeCount = getLikeCount(storyId);
        long commentCount = getCommentCount(storyId);
        
        return StoryResponse.fromStory(story, false, false, likeCount, commentCount); // Default to false when no user context
    }
    
    /**
     * Like a story
     * @param userId The user ID who is liking the story
     * @param storyId The story ID to like
     * @return LikeResponse with like details
     */
    public LikeResponse likeStory(String userId, String storyId) {
        log.info("User {} liking story {}", userId, storyId);
        
        // Check if story exists
        if (!storyRepository.existsById(storyId)) {
            throw new RuntimeException("Story not found with ID: " + storyId);
        }
        
        // Check if already liked
        if (likeService.isLiked(userId, storyId)) {
            throw new RuntimeException("Story already liked by user");
        }
        
        // Create like
        LikeRequest likeRequest = LikeRequest.builder()
                .storyId(storyId)
                .build();
        
        LikeResponse likeResponse = likeService.createLike(userId, likeRequest);
        log.info("Story {} liked successfully by user {}", storyId, userId);
        
        return likeResponse;
    }
    
    /**
     * Unlike a story
     * @param userId The user ID who is unliking the story
     * @param storyId The story ID to unlike
     */
    public void unlikeStory(String userId, String storyId) {
        log.info("User {} unliking story {}", userId, storyId);
        
        // Check if story exists
        if (!storyRepository.existsById(storyId)) {
            throw new RuntimeException("Story not found with ID: " + storyId);
        }
        
        // Check if liked
        if (!likeService.isLiked(userId, storyId)) {
            throw new RuntimeException("Story not liked by user");
        }
        
        // Remove like
        likeService.deleteLikeByUserAndStory(userId, storyId);
        log.info("Story {} unliked successfully by user {}", storyId, userId);
    }
    
    /**
     * Get like count for a story
     * @param storyId The story ID
     * @return Number of likes
     */
    public long getLikeCount(String storyId) {
        return likeService.getLikeCount(storyId);
    }
    
    /**
     * Get stories liked by a user
     * @param userId The user ID
     * @param page Page number
     * @param size Page size
     * @return PagedResponse of liked stories
     */
    public PagedResponse<StoryResponse> getLikedStories(String userId, int page, int size) {
        PagedResponse<LikeResponse> likedStories = likeService.getLikesByUser(userId, page, size);
        
        List<StoryResponse> stories = likedStories.getContent().stream()
                .map(like -> {
                    Story story = storyRepository.findById(like.getStoryId())
                            .orElse(null);
                    return story != null ? StoryResponse.fromStory(story) : null;
                })
                .filter(story -> story != null)
                .collect(Collectors.toList());
        
        return PagedResponse.of(stories, page, size, likedStories.getTotalElements());
    }
    
    /**
     * Get comments for a story
     * @param storyId The story ID
     * @param page Page number
     * @param size Page size
     * @return PagedResponse of comments with nested replies
     */
    public PagedResponse<CommentResponse> getStoryComments(String storyId, int page, int size) {
        log.info("Getting comments for story {} (page: {}, size: {})", storyId, page, size);
        
        // Check if story exists
        if (!storyRepository.existsById(storyId)) {
            throw new RuntimeException("Story not found with ID: " + storyId);
        }
        
        return commentService.getCommentsByStory(storyId, page, size);
    }
    
    /**
     * Get all comments for a story (including replies)
     * @param storyId The story ID
     * @return List of all comments with nested replies
     */
    public List<CommentResponse> getAllStoryComments(String storyId) {
        log.info("Getting all comments for story {}", storyId);
        
        // Check if story exists
        if (!storyRepository.existsById(storyId)) {
            throw new RuntimeException("Story not found with ID: " + storyId);
        }
        
        return commentService.getAllCommentsByStory(storyId);
    }
    
    /**
     * Get comment count for a story
     * @param storyId The story ID
     * @return Total number of comments (including replies)
     */
    public long getCommentCount(String storyId) {
        return commentService.getCommentCount(storyId);
    }
    
    /**
     * Add a comment to a story
     * @param userId The user ID who is commenting
     * @param storyId The story ID to comment on
     * @param text The comment text
     * @param parentId Optional parent comment ID for replies
     * @return CommentResponse with comment details
     */
    public CommentResponse addComment(String userId, String storyId, String text, String parentId) {
        log.info("User {} adding comment to story {}", userId, storyId);
        
        // Check if story exists
        if (!storyRepository.existsById(storyId)) {
            throw new RuntimeException("Story not found with ID: " + storyId);
        }
        
        // If this is a reply, check if parent comment exists
        if (parentId != null && !parentId.isEmpty()) {
            try {
                commentService.getCommentById(parentId);
            } catch (Exception e) {
                throw new RuntimeException("Parent comment not found with ID: " + parentId);
            }
        }
        
        CommentRequest commentRequest = CommentRequest.builder()
                .storyId(storyId)
                .text(text)
                .parentId(parentId)
                .build();
        
        CommentResponse commentResponse = commentService.createComment(userId, commentRequest);
        log.info("Comment added successfully to story {}", storyId);
        
        return commentResponse;
    }
    
    /**
     * Update a comment
     * @param userId The user ID who owns the comment
     * @param commentId The comment ID to update
     * @param text The new comment text
     * @return Updated CommentResponse
     */
    public CommentResponse updateComment(String userId, String commentId, String text) {
        log.info("User {} updating comment {}", userId, commentId);
        
        CommentRequest commentRequest = CommentRequest.builder()
                .text(text)
                .build();
        
        return commentService.updateComment(commentId, userId, commentRequest);
    }
    
    /**
     * Delete a comment
     * @param userId The user ID who owns the comment
     * @param commentId The comment ID to delete
     */
    public void deleteComment(String userId, String commentId) {
        log.info("User {} deleting comment {}", userId, commentId);
        commentService.deleteComment(commentId, userId);
    }
    
    public void incrementViewCount(String storyId) {
        storyRepository.findById(storyId).ifPresent(story -> {
            story.setViewCount(story.getViewCount() != null ? story.getViewCount() + 1 : 1);
            storyRepository.save(story);
        });
    }
} 
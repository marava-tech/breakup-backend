package com.breakupstories.service;

import com.breakupstories.dto.PagedResponse;
import com.breakupstories.dto.ShortVideoCommentRequest;
import com.breakupstories.dto.ShortVideoCommentResponse;
import com.breakupstories.model.ShortVideo;
import com.breakupstories.model.ShortVideoComment;
import com.breakupstories.model.ShortVideoInteraction;
import com.breakupstories.model.ShortVideoLike;
import com.breakupstories.model.User;
import com.breakupstories.repository.ShortVideoCommentRepository;
import com.breakupstories.repository.ShortVideoInteractionRepository;
import com.breakupstories.repository.ShortVideoLikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShortVideoInteractionService {

    private final ShortVideoLikeRepository likeRepository;
    private final ShortVideoCommentRepository commentRepository;
    private final ShortVideoInteractionRepository interactionRepository;
    private final UserService userService;
    private final MongoTemplate mongoTemplate;

    public void likeVideo(String userId, String videoId) {
        if (!likeRepository.existsByUserIdAndVideoId(userId, videoId)) {
            ShortVideoLike like = ShortVideoLike.builder()
                    .userId(userId)
                    .videoId(videoId)
                    .build();
            likeRepository.save(like);

            // Increment like count atomically
            Query query = new Query(Criteria.where("id").is(videoId));
            Update update = new Update().inc("likeCount", 1);
            mongoTemplate.updateFirst(query, update, ShortVideo.class);
        }
    }

    public void unlikeVideo(String userId, String videoId) {
        likeRepository.findByUserIdAndVideoId(userId, videoId).ifPresent(like -> {
            likeRepository.delete(like);

            // Decrement like count atomically
            Query query = new Query(Criteria.where("id").is(videoId));
            Update update = new Update().inc("likeCount", -1);
            mongoTemplate.updateFirst(query, update, ShortVideo.class);
        });
    }

    public ShortVideoCommentResponse addComment(String userId, String videoId, ShortVideoCommentRequest request) {
        ShortVideoComment comment = ShortVideoComment.builder()
                .userId(userId)
                .videoId(videoId)
                .text(request.getText())
                .parentId(request.getParentId())
                .active(true)
                .build();

        ShortVideoComment saved = commentRepository.save(comment);

        // Increment comment count atomically
        Query query = new Query(Criteria.where("id").is(videoId));
        Update update = new Update().inc("commentCount", 1);
        mongoTemplate.updateFirst(query, update, ShortVideo.class);

        User user = userService.getUserEntityById(userId);
        return ShortVideoCommentResponse.fromCommentAndUser(saved, user);
    }

    public PagedResponse<ShortVideoCommentResponse> getComments(String videoId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ShortVideoComment> commentsPage = commentRepository.findByVideoIdAndActiveTrueAndParentIdIsNull(videoId,
                pageable);

        List<ShortVideoCommentResponse> responses = commentsPage.getContent().stream()
                .map(comment -> {
                    User user = userService.getUserEntityById(comment.getUserId());
                    return ShortVideoCommentResponse.fromCommentAndUser(comment, user);
                })
                .collect(Collectors.toList());

        return PagedResponse.of(responses, page, size, commentsPage.getTotalElements());
    }

    public void deleteComment(String userId, String commentId) {
        commentRepository.findById(commentId).ifPresent(comment -> {
            // Verify ownership
            if (!comment.getUserId().equals(userId)) {
                throw new RuntimeException("Unauthorized: Cannot delete comment belonging to another user");
            }

            if (!comment.isActive()) {
                return;
            }

            comment.setActive(false);
            commentRepository.save(comment);

            int deletedCount = 1;

            // Also delete replies if any
            List<ShortVideoComment> replies = commentRepository.findByParentId(commentId);
            for (ShortVideoComment reply : replies) {
                if (reply.isActive()) {
                    reply.setActive(false);
                    commentRepository.save(reply);
                    deletedCount++;
                }
            }

            // Decrement comment count atomically
            Query query = new Query(Criteria.where("id").is(comment.getVideoId()));
            Update update = new Update().inc("commentCount", -deletedCount);
            mongoTemplate.updateFirst(query, update, ShortVideo.class);
        });
    }

    public void recordView(String userId, String videoId) {
        // Record viewer interaction only if user is logged in
        if (userId != null) {
            interactionRepository
                    .findByUserIdAndVideoIdAndType(userId, videoId, ShortVideoInteraction.InteractionType.VIEW)
                    .ifPresentOrElse(
                            interaction -> {
                            }, // Already recorded
                            () -> {
                                ShortVideoInteraction interaction = ShortVideoInteraction.builder()
                                        .userId(userId)
                                        .videoId(videoId)
                                        .type(ShortVideoInteraction.InteractionType.VIEW)
                                        .build();
                                interactionRepository.save(interaction);
                            });
        }

        // Increment global view count
        Query query = new Query(Criteria.where("id").is(videoId));
        Update update = new Update().inc("viewCount", 1);
        mongoTemplate.updateFirst(query, update, ShortVideo.class);
    }

    public void recordShare(String userId, String videoId) {
        if (userId != null) {
            ShortVideoInteraction interaction = ShortVideoInteraction.builder()
                    .userId(userId)
                    .videoId(videoId)
                    .type(ShortVideoInteraction.InteractionType.SHARE)
                    .build();
            interactionRepository.save(interaction);
        }

        // Increment share count
        Query query = new Query(Criteria.where("id").is(videoId));
        Update update = new Update().inc("shareCount", 1);
        mongoTemplate.updateFirst(query, update, ShortVideo.class);
    }

}

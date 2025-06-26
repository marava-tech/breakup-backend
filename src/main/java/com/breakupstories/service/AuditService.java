package com.breakupstories.service;

import com.breakupstories.dto.AuditRequest;
import com.breakupstories.dto.AuditResponse;
import com.breakupstories.dto.PagedResponse;
import com.breakupstories.model.Audit;
import com.breakupstories.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditService {
    
    private final AuditRepository auditRepository;
    
    public AuditResponse createAudit(AuditRequest request) {
        Audit audit = Audit.builder()
                .userId(request.getUserId())
                .entityType(request.getEntityType())
                .actionType(request.getActionType())
                .entityId(request.getEntityId())
                .userAgent(request.getUserAgent())
                .ipAddress(request.getIpAddress())
                .sessionId(request.getSessionId())
                .metadata(request.getMetadata())
                .build();
        
        Audit savedAudit = auditRepository.save(audit);
        return AuditResponse.fromAudit(savedAudit);
    }
    
    public void logAudit(String userId, Audit.EntityType entityType, Audit.ActionType actionType, String entityId) {
        logAudit(userId, entityType, actionType, entityId, null, null, null, null);
    }
    
    public void logAudit(String userId, Audit.EntityType entityType, Audit.ActionType actionType, String entityId, 
                        String userAgent, String ipAddress, String sessionId, Map<String, Object> metadata) {
        Audit audit = Audit.builder()
                .userId(userId)
                .entityType(entityType)
                .actionType(actionType)
                .entityId(entityId)
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .sessionId(sessionId)
                .metadata(metadata)
                .build();
        
        auditRepository.save(audit);
    }
    
    // Convenience methods for common interactions
    public void logStoryView(String userId, String storyId, String userAgent, String ipAddress, String sessionId) {
        Map<String, Object> metadata = Map.of("interaction_type", "story_view");
        logAudit(userId, Audit.EntityType.STORY, Audit.ActionType.VIEW, storyId, userAgent, ipAddress, sessionId, metadata);
    }
    
    public void logStoryLike(String userId, String storyId, String userAgent, String ipAddress, String sessionId) {
        Map<String, Object> metadata = Map.of("interaction_type", "story_like");
        logAudit(userId, Audit.EntityType.STORY, Audit.ActionType.LIKE, storyId, userAgent, ipAddress, sessionId, metadata);
    }
    
    public void logStoryUnlike(String userId, String storyId, String userAgent, String ipAddress, String sessionId) {
        Map<String, Object> metadata = Map.of("interaction_type", "story_unlike");
        logAudit(userId, Audit.EntityType.STORY, Audit.ActionType.UNLIKE, storyId, userAgent, ipAddress, sessionId, metadata);
    }
    
    public void logCommentCreate(String userId, String commentId, String storyId, String userAgent, String ipAddress, String sessionId) {
        Map<String, Object> metadata = Map.of(
            "interaction_type", "comment_create",
            "story_id", storyId
        );
        logAudit(userId, Audit.EntityType.COMMENT, Audit.ActionType.CREATE, commentId, userAgent, ipAddress, sessionId, metadata);
    }
    
    public void logAudioPlay(String userId, String storyId, String userAgent, String ipAddress, String sessionId, 
                           Long duration, Long position) {
        Map<String, Object> metadata = Map.of(
            "interaction_type", "audio_play",
            "duration", duration,
            "position", position
        );
        logAudit(userId, Audit.EntityType.AUDIO, Audit.ActionType.PLAY, storyId, userAgent, ipAddress, sessionId, metadata);
    }
    
    public void logAudioPause(String userId, String storyId, String userAgent, String ipAddress, String sessionId, 
                            Long duration, Long position) {
        Map<String, Object> metadata = Map.of(
            "interaction_type", "audio_pause",
            "duration", duration,
            "position", position
        );
        logAudit(userId, Audit.EntityType.AUDIO, Audit.ActionType.PAUSE, storyId, userAgent, ipAddress, sessionId, metadata);
    }
    
    public void logAudioStop(String userId, String storyId, String userAgent, String ipAddress, String sessionId, 
                           Long duration, Long position) {
        Map<String, Object> metadata = Map.of(
            "interaction_type", "audio_stop",
            "duration", duration,
            "position", position
        );
        logAudit(userId, Audit.EntityType.AUDIO, Audit.ActionType.STOP, storyId, userAgent, ipAddress, sessionId, metadata);
    }
    
    public void logBookmarkCreate(String userId, String storyId, String userAgent, String ipAddress, String sessionId) {
        Map<String, Object> metadata = Map.of("interaction_type", "bookmark_create");
        logAudit(userId, Audit.EntityType.BOOKMARK, Audit.ActionType.CREATE, storyId, userAgent, ipAddress, sessionId, metadata);
    }
    
    public void logBookmarkDelete(String userId, String storyId, String userAgent, String ipAddress, String sessionId) {
        Map<String, Object> metadata = Map.of("interaction_type", "bookmark_delete");
        logAudit(userId, Audit.EntityType.BOOKMARK, Audit.ActionType.DELETE, storyId, userAgent, ipAddress, sessionId, metadata);
    }
    
    public PagedResponse<AuditResponse> getAudits(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Audit> auditPage = auditRepository.findAll(pageable);
        
        List<AuditResponse> audits = auditPage.getContent().stream()
                .map(AuditResponse::fromAudit)
                .collect(Collectors.toList());
        
        return PagedResponse.of(audits, page, size, auditPage.getTotalElements());
    }
    
    public PagedResponse<AuditResponse> getAuditsByUser(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Audit> auditPage = auditRepository.findByUserId(userId, pageable);
        
        List<AuditResponse> audits = auditPage.getContent().stream()
                .map(AuditResponse::fromAudit)
                .collect(Collectors.toList());
        
        return PagedResponse.of(audits, page, size, auditPage.getTotalElements());
    }
    
    public PagedResponse<AuditResponse> getAuditsByEntityType(Audit.EntityType entityType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Audit> auditPage = auditRepository.findByEntityType(entityType, pageable);
        
        List<AuditResponse> audits = auditPage.getContent().stream()
                .map(AuditResponse::fromAudit)
                .collect(Collectors.toList());
        
        return PagedResponse.of(audits, page, size, auditPage.getTotalElements());
    }
    
    public PagedResponse<AuditResponse> getAuditsByEntityId(String entityId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Audit> auditPage = auditRepository.findByEntityId(entityId, pageable);
        
        List<AuditResponse> audits = auditPage.getContent().stream()
                .map(AuditResponse::fromAudit)
                .collect(Collectors.toList());
        
        return PagedResponse.of(audits, page, size, auditPage.getTotalElements());
    }
    
    public PagedResponse<AuditResponse> getAuditsByActionType(Audit.ActionType actionType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Audit> auditPage = auditRepository.findByActionType(actionType, pageable);
        
        List<AuditResponse> audits = auditPage.getContent().stream()
                .map(AuditResponse::fromAudit)
                .collect(Collectors.toList());
        
        return PagedResponse.of(audits, page, size, auditPage.getTotalElements());
    }
    
    public PagedResponse<AuditResponse> getAuditsByUserAndEntityType(String userId, Audit.EntityType entityType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Audit> auditPage = auditRepository.findByUserIdAndEntityType(userId, entityType, pageable);
        
        List<AuditResponse> audits = auditPage.getContent().stream()
                .map(AuditResponse::fromAudit)
                .collect(Collectors.toList());
        
        return PagedResponse.of(audits, page, size, auditPage.getTotalElements());
    }
    
    public AuditResponse getAuditById(String auditId) {
        Audit audit = auditRepository.findById(auditId)
                .orElseThrow(() -> new RuntimeException("Audit not found with ID: " + auditId));
        
        return AuditResponse.fromAudit(audit);
    }
    
    public void deleteAudit(String auditId) {
        if (!auditRepository.existsById(auditId)) {
            throw new RuntimeException("Audit not found with ID: " + auditId);
        }
        
        auditRepository.deleteById(auditId);
    }
} 
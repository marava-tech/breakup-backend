package com.breakupstories.service;

import com.breakupstories.dto.AudioInfoResponse;
import com.breakupstories.dto.StoryResponse;
import com.breakupstories.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AudioStreamServiceImpl implements AudioStreamService {
    
    private final StoryService storyService;
    
    // In-memory cache for audio info to avoid repeated URL validation
    private final ConcurrentHashMap<String, AudioInfoResponse> audioInfoCache = new ConcurrentHashMap<>();
    
    @Override
    public ResponseEntity<Resource> streamAudio(String storyId, String rangeHeader) {
        log.info("Audio stream request for story: {} with range: {}", storyId, rangeHeader);
        
        try {
            // Get story and validate audio URL
            StoryResponse story = storyService.getStoryById(storyId);
            if (story.getAudioUrl() == null || story.getAudioUrl().isEmpty()) {
                log.error("Story {} has no audio URL", storyId);
                throw new ResourceNotFoundException("Story", "audioUrl", "Story has no audio URL");
            }
            
            // Create resource from URL
            URL audioUrl = new URL(story.getAudioUrl());
            Resource resource = new UrlResource(audioUrl);
            
            if (!resource.exists()) {
                log.error("Audio resource not found for story: {} at URL: {}", storyId, story.getAudioUrl());
                throw new ResourceNotFoundException("Audio", "url", story.getAudioUrl());
            }
            
            // Handle range requests for partial content
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                return handleRangeRequest(resource, rangeHeader, story.getAudioUrl());
            }
            
            // Full content request
            HttpHeaders headers = createHeaders(resource, story.getAudioUrl());
            
            log.info("Streaming full audio for story: {} ({} bytes)", storyId, resource.contentLength());
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Error streaming audio for story {}: {}", storyId, e.getMessage(), e);
            throw new RuntimeException("Failed to stream audio: " + e.getMessage(), e);
        }
    }
    
    @Override
    public AudioInfoResponse getAudioInfo(String storyId) {
        log.info("Audio info request for story: {}", storyId);
        
        // Check cache first
        AudioInfoResponse cachedInfo = audioInfoCache.get(storyId);
        if (cachedInfo != null) {
            log.debug("Returning cached audio info for story: {}", storyId);
            return cachedInfo;
        }
        
        try {
            StoryResponse story = storyService.getStoryById(storyId);
            if (story.getAudioUrl() == null || story.getAudioUrl().isEmpty()) {
                throw new ResourceNotFoundException("Story", "audioUrl", "Story has no audio URL");
            }
            
            URL audioUrl = new URL(story.getAudioUrl());
            Resource resource = new UrlResource(audioUrl);
            
            if (!resource.exists()) {
                throw new ResourceNotFoundException("Audio", "url", story.getAudioUrl());
            }
            
            AudioInfoResponse info = AudioInfoResponse.builder()
                    .storyId(storyId)
                    .audioUrl(story.getAudioUrl())
                    .contentLength(resource.contentLength())
                    .contentType(determineContentType(story.getAudioUrl()))
                    .supportsRangeRequests(true)
                    .build();
            
            // Cache the info
            audioInfoCache.put(storyId, info);
            
            log.info("Audio info for story {}: {} bytes, type: {}", 
                storyId, info.getContentLength(), info.getContentType());
            
            return info;
            
        } catch (Exception e) {
            log.error("Error getting audio info for story {}: {}", storyId, e.getMessage(), e);
            throw new RuntimeException("Failed to get audio info: " + e.getMessage(), e);
        }
    }
    
    private ResponseEntity<Resource> handleRangeRequest(Resource resource, String rangeHeader, String audioUrl) throws IOException {
        long contentLength = resource.contentLength();
        String[] ranges = rangeHeader.substring("bytes=".length()).split("-");
        
        long start = Long.parseLong(ranges[0]);
        long end = ranges.length > 1 && !ranges[1].isEmpty() 
                ? Long.parseLong(ranges[1]) 
                : contentLength - 1;
        
        // Validate range
        if (start >= contentLength || end >= contentLength || start > end) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .header("Content-Range", "bytes */" + contentLength)
                    .build();
        }
        
        long rangeLength = end - start + 1;
        
        // Create partial resource
        Resource partialResource = new PartialResource(resource, start, end);
        
        HttpHeaders headers = createHeaders(resource, audioUrl);
        headers.setContentLength(rangeLength);
        headers.set("Content-Range", String.format("bytes %d-%d/%d", start, end, contentLength));
        
        log.debug("Streaming partial audio: bytes {}-{} of {} ({} bytes)", 
            start, end, contentLength, rangeLength);
        
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .headers(headers)
                .body(partialResource);
    }
    
    private HttpHeaders createHeaders(Resource resource, String audioUrl) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(determineContentType(audioUrl));
        headers.setContentLength(resource.contentLength());
        headers.set("Accept-Ranges", "bytes");
        headers.set("Cache-Control", "public, max-age=3600"); // Cache for 1 hour
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET, HEAD, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Range");
        return headers;
    }
    
    private MediaType determineContentType(String audioUrl) {
        String lowerUrl = audioUrl.toLowerCase();
        if (lowerUrl.endsWith(".mp3")) {
            return MediaType.valueOf("audio/mpeg");
        } else if (lowerUrl.endsWith(".wav")) {
            return MediaType.valueOf("audio/wav");
        } else if (lowerUrl.endsWith(".m4a") || lowerUrl.endsWith(".mp4")) {
            return MediaType.valueOf("audio/mp4");
        } else if (lowerUrl.endsWith(".ogg")) {
            return MediaType.valueOf("audio/ogg");
        } else if (lowerUrl.endsWith(".aac")) {
            return MediaType.valueOf("audio/aac");
        } else {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
    
    // Inner class for partial resource handling
    private static class PartialResource implements Resource {
        private final Resource delegate;
        private final long start;
        private final long end;
        
        public PartialResource(Resource delegate, long start, long end) {
            this.delegate = delegate;
            this.start = start;
            this.end = end;
        }
        
        @Override
        public java.io.InputStream getInputStream() throws IOException {
            java.io.InputStream inputStream = delegate.getInputStream();
            inputStream.skip(start);
            return new LimitedInputStream(inputStream, end - start + 1);
        }
        
        // Implement other Resource methods by delegating to the original resource
        @Override
        public boolean exists() { return delegate.exists(); }
        
        @Override
        public boolean isReadable() { return delegate.isReadable(); }
        
        @Override
        public boolean isOpen() { return delegate.isOpen(); }
        
        @Override
        public boolean isFile() { return delegate.isFile(); }
        
        @Override
        public java.net.URL getURL() throws IOException { return delegate.getURL(); }
        
        @Override
        public java.net.URI getURI() throws IOException { return delegate.getURI(); }
        
        @Override
        public java.io.File getFile() throws IOException { return delegate.getFile(); }
        
        @Override
        public long contentLength() throws IOException { return end - start + 1; }
        
        @Override
        public long lastModified() throws IOException { return delegate.lastModified(); }
        
        @Override
        public Resource createRelative(String relativePath) throws IOException { 
            return delegate.createRelative(relativePath); 
        }
        
        @Override
        public String getFilename() { return delegate.getFilename(); }
        
        @Override
        public String getDescription() { return delegate.getDescription(); }
    }
    
    // Inner class for limiting input stream
    private static class LimitedInputStream extends java.io.InputStream {
        private final java.io.InputStream delegate;
        private final long limit;
        private long position = 0;
        
        public LimitedInputStream(java.io.InputStream delegate, long limit) {
            this.delegate = delegate;
            this.limit = limit;
        }
        
        @Override
        public int read() throws IOException {
            if (position >= limit) {
                return -1;
            }
            int result = delegate.read();
            if (result != -1) {
                position++;
            }
            return result;
        }
        
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (position >= limit) {
                return -1;
            }
            int maxRead = (int) Math.min(len, limit - position);
            int result = delegate.read(b, off, maxRead);
            if (result != -1) {
                position += result;
            }
            return result;
        }
        
        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }
} 
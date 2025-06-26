package com.breakupstories.controller;

import com.breakupstories.dto.UploadResponse;
import com.breakupstories.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "File Upload", description = "File upload APIs")
public class UploadController {
    
    private final UploadService uploadService;
    
    @PostMapping("/file")
    @Operation(summary = "Upload single file", description = "Upload a single file to the external service")
    public ResponseEntity<UploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("Received single file upload request: {} ({} bytes)", 
            file.getOriginalFilename(), file.getSize());
        
        UploadResponse response = uploadService.uploadFile(file);
        
        log.info("Single file upload completed successfully: {} -> {} URLs", 
            file.getOriginalFilename(), response.getData().size());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/files")
    @Operation(summary = "Upload multiple files", description = "Upload multiple files to the external service")
    public ResponseEntity<UploadResponse> uploadFiles(@RequestParam("files") List<MultipartFile> files) {
        log.info("Received multiple files upload request: {} files", files.size());
        
        UploadResponse response = uploadService.uploadFiles(files);
        
        log.info("Multiple files upload completed successfully: {} files -> {} URLs", 
            files.size(), response.getData().size());
        
        return ResponseEntity.ok(response);
    }
} 
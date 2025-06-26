package com.breakupstories.service;

import com.breakupstories.dto.UploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UploadService {
    
    /**
     * Upload a single file to the external service
     * @param file The file to upload
     * @return UploadResponse containing the uploaded URLs
     */
    UploadResponse uploadFile(MultipartFile file);
    
    /**
     * Upload multiple files to the external service
     * @param files List of files to upload
     * @return UploadResponse containing the uploaded URLs
     */
    UploadResponse uploadFiles(List<MultipartFile> files);
} 
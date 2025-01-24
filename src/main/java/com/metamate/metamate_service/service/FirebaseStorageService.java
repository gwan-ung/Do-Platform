package com.metamate.metamate_service.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FirebaseStorageService {
    String uploadFile(MultipartFile file, String fileName) throws IOException;
    byte[] downloadFile(String fileName) throws IOException;
    void deleteFile(String fileName) throws IOException;
}

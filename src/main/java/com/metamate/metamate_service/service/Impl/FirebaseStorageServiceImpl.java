package com.metamate.metamate_service.service.Impl;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import com.metamate.metamate_service.service.FirebaseStorageService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class FirebaseStorageServiceImpl implements FirebaseStorageService {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Override
    public String uploadFile(MultipartFile file, String fileName) throws IOException {

        logger.debug("FirebaseStorageServiceImpl :: uploadFile :: {}",fileName);
        if (fileName == null) {
            throw new IOException("File name is null");
        }
        String fullPath = "QuizImage/" + fileName;
        Bucket bucket = StorageClient.getInstance().bucket();
        Blob blob = bucket.create(fullPath, file.getInputStream(), file.getContentType());
        // 경로 포함된 파일명 생성

        String url =  String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                bucket.getName(),
                fullPath.replace("/", "%2F")); // URL 인코딩 처리
        logger.debug("FirebaseStorageServiceImpl :: uploadFile :: url = {}",url);
        return url;
    }

    @Override
    public byte[] downloadFile(String fileName) throws IOException {

        logger.debug("FirebaseStorageServiceImpl :: downloadFile :: {}",fileName);
        // 경로 포함된 파일명 생성
        String fullPath = "QuizImage/" + fileName;
        Bucket bucket = StorageClient.getInstance().bucket();
        Blob blob = bucket.get(fullPath);

        if (blob == null) {
            throw new IOException("File not found in Firebase Storage");
        }

        return blob.getContent();
    }

    @Override
    public void deleteFile(String fileName) throws IOException {
        logger.debug("FirebaseStorageServiceImpl :: deleteFile :: {}", fileName);
        if (fileName == null || fileName.isEmpty()) {
            throw new IOException("File name is null or empty");
        }

        // 경로 포함된 파일명 (예: QuizImage/file-name)
        String fullPath = "QuizImage/" + fileName;

        // Firebase Storage의 Bucket 인스턴스 가져오기
        Bucket bucket = StorageClient.getInstance().bucket();

        // Blob 삭제
        boolean deleted = bucket.get(fullPath).delete();

        if (deleted) {
            logger.info("FirebaseStorageServiceImpl :: deleteFile :: File successfully deleted: {}", fullPath);
        } else {
            throw new IOException("Failed to delete file: " + fullPath);
        }
    }
}

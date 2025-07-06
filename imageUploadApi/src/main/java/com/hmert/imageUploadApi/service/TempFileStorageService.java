package com.hmert.imageUploadApi.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class TempFileStorageService {
    private final String BASE_UPLOAD_DIR = "/tmp/uploads";

    public Path saveTempImage(UUID sessionId, UUID imageId, MultipartFile image) throws IOException{
        Path sessionDir = Paths.get(BASE_UPLOAD_DIR, sessionId.toString());
        Files.createDirectories(sessionDir);

        Path imagePath = sessionDir.resolve(imageId + ".jpg");
        image.transferTo(imagePath.toFile());
        return imagePath;
    }

    public boolean deleteTempImage(UUID sessionId, UUID imageId) {
        Path sessionDir = Paths.get(BASE_UPLOAD_DIR, sessionId.toString());
        Path imagePath = sessionDir.resolve(imageId + ".jpg");
        try{
            return Files.deleteIfExists(imagePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

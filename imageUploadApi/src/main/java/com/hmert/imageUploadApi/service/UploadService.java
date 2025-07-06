package com.hmert.imageUploadApi.service;

import com.hmert.imageUploadApi.client.ImageProcessorClient;
import com.hmert.imageUploadApi.redis.RedisSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class UploadService {

    // constructor injection instead of Autowired
    private final RedisSessionRepository redisSessionRepository;
    private final ImageProcessorClient imageProcessorClient;
    private final TempFileStorageService tempFileStorageService;

    private static final int MAX_IMAGES_PER_SESSION = 10;
    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final int THROTTLE_WINDOW_SECONDS = 60;

    public UploadService(RedisSessionRepository redisSessionRepository, ImageProcessorClient imageProcessorClient, TempFileStorageService tempFileStorageService) {
        this.redisSessionRepository = redisSessionRepository;
        this.imageProcessorClient = imageProcessorClient;
        this.tempFileStorageService = tempFileStorageService;
    }

    public void processUpload(MultipartFile image, String sessionId, String ip, String endpoint) {
        // 1. UUID validasyonu
        validateUUID(sessionId);

        // 2. IP başına rate limiting
        String rateKey = String.format("ratelimit:%s:%s", ip, endpoint);
        Long ipReqCount = redisSessionRepository.incrementWithTTL(rateKey, THROTTLE_WINDOW_SECONDS);
        if (ipReqCount > MAX_REQUESTS_PER_MINUTE) {
            throw new IllegalStateException("Rate limit exceeded for IP");
        }

        // 3. Session geçerli mi?
        if (!redisSessionRepository.isSessionValid(sessionId)) {
            throw new IllegalStateException("Session expired or invalid");
        }

        // 4. Görsel sayısı kontrolü
        Long imageCount = redisSessionRepository.incrementImageCounter(sessionId);
        System.out.printf("Image count for session %s: %d%n", sessionId, imageCount);
        if (imageCount > MAX_IMAGES_PER_SESSION) {
            throw new IllegalStateException("Image upload limit exceeded for this session");
        }

        // 5. Image kaydı (imageId UUID)
        String imageId = UUID.randomUUID().toString();
        System.out.printf("Image %s accepted for session %s, file: %s%n", imageId, sessionId, image.getOriginalFilename());
        redisSessionRepository.addImageToSession(sessionId, imageId);

        Path savedImagePath;
        try {
            savedImagePath = tempFileStorageService.saveTempImage(UUID.fromString(sessionId), UUID.fromString(imageId), image);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.printf("Image %s saved to %s%n", imageId, savedImagePath);
        System.out.printf("Image %s saved to %s%n", imageId, savedImagePath.toString());
        imageProcessorClient.sendImageToProcessor(savedImagePath.toString(), savedImagePath);
    }

    private void validateUUID(String id) {
        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format: " + id);
        }
    }
}

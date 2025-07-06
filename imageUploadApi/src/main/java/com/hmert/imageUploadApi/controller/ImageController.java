package com.hmert.imageUploadApi.controller;

import com.hmert.imageUploadApi.redis.RedisSessionRepository;
import com.hmert.imageUploadApi.service.TempFileStorageService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/sessions")
public class ImageController {
    private final RedisSessionRepository redis;
    private final TempFileStorageService tempFileStorageService;

    public ImageController(RedisSessionRepository redis, TempFileStorageService tempFileStorageService) {
        this.redis = redis;
        this.tempFileStorageService = tempFileStorageService;
    }

    @GetMapping("/{sessionId}/images")
    public ResponseEntity<Set<String>> listImages(
            @PathVariable("sessionId") @NotBlank String sessionId
    ) {
        validateUUID(sessionId);

        if (!redis.isSessionValid(sessionId)) {
            return ResponseEntity.badRequest().build();
        }

        Set<String> imageIds = redis.getAllImagesForSession(sessionId);
        return ResponseEntity.ok(imageIds);
    }

    @DeleteMapping("/{sessionId}/images/{imageId}")
    public ResponseEntity<String> deleteImage(
            @PathVariable("sessionId") @NotBlank String sessionId,
            @PathVariable("imageId") @NotBlank String imageId
    ) {
        validateUUID(sessionId);
        validateUUID(imageId);

        if (!redis.isSessionValid(sessionId)) {
            return ResponseEntity.badRequest().body("Invalid session ID");
        }

        boolean removed = redis.removeImageFromSession(sessionId, imageId);
        if (removed) {
            tempFileStorageService.deleteTempImage(UUID.fromString(sessionId), UUID.fromString(imageId));
            return ResponseEntity.ok("Image deleted");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private void validateUUID(String id) {
        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format: " + id);
        }
    }
}

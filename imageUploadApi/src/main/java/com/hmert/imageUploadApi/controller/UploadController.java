package com.hmert.imageUploadApi.controller;

import com.hmert.imageUploadApi.service.UploadService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/sessions")
public class UploadController {
    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping("/{sessionId}/images")
    public ResponseEntity<String> uploadImage(
            @PathVariable("sessionId") @NotBlank String sessionId,
            @RequestParam("image") MultipartFile image,
            HttpServletRequest request
    ) {
        String clientIp = request.getRemoteAddr();
        uploadService.processUpload(image, sessionId, clientIp, "/sessions/{sessionId}/images");
        return ResponseEntity.ok("Image uploaded successfully");
    }
}

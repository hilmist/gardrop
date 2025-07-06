package com.hmert.imageProcessingApi.controller;

import com.hmert.imageProcessingApi.dto.ProcessResult;
import com.hmert.imageProcessingApi.service.ImageProcessorService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/images")
public class ImageProcessorController {
    private final ImageProcessorService processorService;

    public ImageProcessorController(ImageProcessorService processorService) {
        this.processorService = processorService;
    }

    @PostMapping("/process")
    public ResponseEntity<ProcessResult> processImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam("destinationFilePath") @NotBlank String destinationFilePath
    ) {
        ProcessResult result = processorService.process(image, destinationFilePath);
        return ResponseEntity.ok(result);
    }
}

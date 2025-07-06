package com.hmert.imageProcessingApi.dto;

public record ProcessResult(
        String imageId,
        String path,
        int width,
        int height,
        String status
) {}
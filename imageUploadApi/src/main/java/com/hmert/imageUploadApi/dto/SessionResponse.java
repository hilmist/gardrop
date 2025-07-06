package com.hmert.imageUploadApi.dto;

public record SessionResponse(
        String sessionId,
        String message,
        int expiresInSeconds
) {}

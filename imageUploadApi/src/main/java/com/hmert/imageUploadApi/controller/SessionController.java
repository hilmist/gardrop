package com.hmert.imageUploadApi.controller;

import com.hmert.imageUploadApi.dto.SessionResponse;
import com.hmert.imageUploadApi.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sessions")
public class SessionController {
    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ResponseEntity<SessionResponse> createSession() {
        SessionResponse response = sessionService.createSession();
        return ResponseEntity.ok(response);
    }
}

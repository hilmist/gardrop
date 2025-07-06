package com.hmert.imageUploadApi.service;

import com.hmert.imageUploadApi.dto.SessionResponse;
import com.hmert.imageUploadApi.redis.RedisSessionRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SessionService {

    // constructor injection instead of Autowired
    private final RedisSessionRepository sessionRepository;

    public SessionService(RedisSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public SessionResponse createSession() {
        String sessionId = UUID.randomUUID().toString();
        sessionRepository.saveSession(sessionId, 60 * 60); // 1 hour TTL

        return new SessionResponse(sessionId, "Session created successfully", 60 * 60);
    }
}

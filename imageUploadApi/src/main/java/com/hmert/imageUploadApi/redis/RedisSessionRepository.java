package com.hmert.imageUploadApi.redis;

import com.hmert.imageUploadApi.service.TempFileStorageService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Repository
public class RedisSessionRepository {
    private final StringRedisTemplate redisTemplate;
    private final TempFileStorageService tempFileStorageService;

    public RedisSessionRepository(StringRedisTemplate redisTemplate, TempFileStorageService tempFileStorageService) {
        this.redisTemplate = redisTemplate;
        this.tempFileStorageService = tempFileStorageService;
    }

    public boolean isSessionValid(String sessionId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("session:" + sessionId));
    }

    public void saveSession(String sessionId, int ttlSeconds) {
        try {
            redisTemplate.opsForValue().set("session:" + sessionId, "active", ttlSeconds, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set("session:" + sessionId + ":images", "0", ttlSeconds, TimeUnit.SECONDS); // image counter reset
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Long incrementImageCounter(String sessionId) {
        try {
            String key = "session:" + sessionId + ":images";
            return redisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Long incrementWithTTL(String key, int ttlSeconds) {
        Long count = null;
        try {
            count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return count;
    }

    public void addImageToSession(String sessionId, String imageId) {
        try {
            String key = "session:" + sessionId + ":images:list";
            redisTemplate.opsForSet().add(key, imageId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //opsForSet() kullanıyoruz çünkü imageId’ler benzersizdir ve set buna çok uygundur.
    public Set<String> getAllImagesForSession(String sessionId) {
        try {
            String key = "session:" + sessionId + ":images:list";
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean removeImageFromSession(String sessionId, String imageId) {
        try {
            String key = "session:" + sessionId + ":images:list";
            String keyForDec = "session:" + sessionId + ":images";
            if(redisTemplate.opsForSet().remove(key, imageId) == 1) {
                redisTemplate.opsForValue().decrement(keyForDec);
                tempFileStorageService.deleteTempImage(UUID.fromString(sessionId), UUID.fromString(imageId));
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

package com.eventflow.eventflow.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class SeatLockService {

    private static final Duration LOCK_DURATION =
            Duration.ofMinutes(10);

    private static final String LOCK_PREFIX =
            "booking:lock:";

    private final RedisTemplate<String, String> redisTemplate;

    public SeatLockService(
            RedisTemplate<String, String> redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    public boolean tryLock(
            UUID eventId,
            UUID seatId,
            String owner
    ) {

        String key = buildKey(eventId, seatId);

        Boolean acquired =
                redisTemplate.opsForValue().setIfAbsent(
                        key,
                        owner,
                        LOCK_DURATION
                );

        return Boolean.TRUE.equals(acquired);
    }

    private String buildKey(
            UUID eventId,
            UUID seatId
    ) {

        return LOCK_PREFIX
                + eventId
                + ":"
                + seatId;
    }
//____________________________________________________________________________
    public boolean releaseLock(
            UUID eventId,
            UUID seatId,
            String owner
    ) {

        String key = buildKey(eventId, seatId);

        String script = """
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1])
            else
                return 0
            end
            """;

        Long result = redisTemplate.execute(
                new DefaultRedisScript<>(script, Long.class),
                List.of(key),
                owner
        );

        return Long.valueOf(1L).equals(result);
    }
}
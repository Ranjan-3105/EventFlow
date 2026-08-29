package com.eventflow.eventflow.service;

import com.eventflow.eventflow.dto.response.EventResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class EventCacheService {

    private static final String PUBLISHED_EVENTS_KEY =
            "cache:events:published";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public EventCacheService(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public List<EventResponse> getPublishedEvents() {

        String cached =
                redisTemplate.opsForValue()
                        .get(PUBLISHED_EVENTS_KEY);

        if (cached == null) {
            return null;
        }

        try {

            return objectMapper.readValue(
                    cached,
                    new TypeReference<List<EventResponse>>() {}
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to deserialize cached events",
                    e
            );
        }
    }

    public void cachePublishedEvents(
        List<EventResponse> events
) {

    try {

        String json =
                objectMapper.writeValueAsString(events);

        redisTemplate.opsForValue()
        .set(
                PUBLISHED_EVENTS_KEY,
                json,
                Duration.ofMinutes(5)
        );

    } catch (Exception e) {

        throw new RuntimeException(
                "Failed to serialize events for cache",
                e
        );
    }
}

    public void invalidatePublishedEvents() {
        redisTemplate.delete(PUBLISHED_EVENTS_KEY);
    }
}
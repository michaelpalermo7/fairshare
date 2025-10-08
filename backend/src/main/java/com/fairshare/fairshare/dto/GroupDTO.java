package com.fairshare.fairshare.dto;

import java.time.Instant;

public record GroupDTO(
        Long id,
        String name,
        Instant createdAt) {
}

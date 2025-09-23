package com.fairshare.fairshare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateGroupRequest(
    @NotBlank String name,
    @NotNull Long creatorUserId
) {}
package com.fairshare.fairshare.dto;

import com.fairshare.fairshare.entity.Membership.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddMemberRequest(
        @NotNull Long userId,
        @NotBlank Role role) {
}

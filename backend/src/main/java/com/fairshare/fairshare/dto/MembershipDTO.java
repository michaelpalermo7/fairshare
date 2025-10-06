package com.fairshare.fairshare.dto;

import com.fairshare.fairshare.entity.Membership;

public record MembershipDTO(
    Long id,
    Long userId,
    Long groupId,
    Membership.Role role
) {}
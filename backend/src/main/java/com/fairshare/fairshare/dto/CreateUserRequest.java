package com.fairshare.fairshare.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @Email String userEmail,
        @NotBlank String userName) {
}

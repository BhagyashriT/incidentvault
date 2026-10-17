package com.incidentvault.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateFixRequest(
        @NotBlank String description,
        String prUrl,
        String commitSha) {
}

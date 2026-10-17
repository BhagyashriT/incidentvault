package com.incidentvault.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateLogSnippetRequest(
        String source,
        @NotBlank String snippet) {
}

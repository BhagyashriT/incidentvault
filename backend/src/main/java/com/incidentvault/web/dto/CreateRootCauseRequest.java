package com.incidentvault.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateRootCauseRequest(
        @NotBlank String hypothesis,
        Boolean validated,
        String evidence) {
}

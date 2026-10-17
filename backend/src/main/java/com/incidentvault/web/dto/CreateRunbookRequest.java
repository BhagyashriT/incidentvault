package com.incidentvault.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateRunbookRequest(
        @NotBlank String title,
        @NotNull @Valid List<StepDto> steps,
        List<Long> linkedIncidentIds) {

    public record StepDto(
            @NotNull Integer order,
            @NotBlank String title,
            String detail,
            String command) {
    }
}

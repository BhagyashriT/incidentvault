package com.incidentvault.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * One step inside a runbook. Serialized to JSON and stored in the runbooks.steps TEXT column.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Step(Integer order, String title, String detail, String command) {
}

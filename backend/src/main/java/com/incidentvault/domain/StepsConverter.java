package com.incidentvault.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Serializes a runbook's ordered steps to a JSON array stored in a TEXT column.
 * Kept portable: plain JSON text, no database-specific JSON type.
 */
@Converter
public class StepsConverter implements AttributeConverter<List<Step>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<Step>> STEP_LIST = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(List<Step> attribute) {
        if (attribute == null) {
            return "[]";
        }
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize runbook steps", e);
        }
    }

    @Override
    public List<Step> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return List.of();
        }
        try {
            return MAPPER.readValue(dbData, STEP_LIST);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize runbook steps", e);
        }
    }
}

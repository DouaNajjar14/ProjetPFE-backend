package com.example.gestion.des.stagiaires.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.MonthDay;

/**
 * Converter for MonthDay <-> String
 * Converts MonthDay to ISO-8601 format (--MM-DD) and back
 */
@Converter(autoApply = true)
public class MonthDayConverter implements AttributeConverter<MonthDay, String> {

    @Override
    public String convertToDatabaseColumn(MonthDay attribute) {
        if (attribute == null) {
            return null;
        }
        // Convert to ISO format: --MM-DD (e.g., --01-07)
        return attribute.toString();
    }

    @Override
    public MonthDay convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        // Parse from ISO format: --MM-DD or MM-DD
        try {
            // Handle both formats: --MM-DD and MM-DD
            if (dbData.startsWith("--")) {
                return MonthDay.parse(dbData);
            } else {
                return MonthDay.parse("--" + dbData);
            }
        } catch (Exception e) {
            // If parsing fails, try direct parse
            return MonthDay.parse(dbData);
        }
    }
}

package com.project.project.dto.auth.deserializer;

import java.io.IOException;
import java.math.BigDecimal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

/**
 * Accepts decimal values sent as number or string and normalizes comma decimals.
 */
public class LenientBigDecimalDeserializer extends StdDeserializer<BigDecimal> {

    public LenientBigDecimalDeserializer() {
        super(BigDecimal.class);
    }

    @Override
    public BigDecimal deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonToken token = parser.currentToken();
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        if (token == JsonToken.VALUE_NUMBER_INT || token == JsonToken.VALUE_NUMBER_FLOAT) {
            return parser.getDecimalValue();
        }
        if (token != JsonToken.VALUE_STRING) {
            throw InvalidFormatException.from(parser, "gpa must be numeric", parser.getText(), BigDecimal.class);
        }

        String raw = parser.getValueAsString();
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if ("nan".equalsIgnoreCase(trimmed) || "infinity".equalsIgnoreCase(trimmed)
                || "-infinity".equalsIgnoreCase(trimmed)) {
            throw InvalidFormatException.from(parser, "gpa must be numeric", raw, BigDecimal.class);
        }

        String normalized = trimmed;
        if (trimmed.contains(",") && !trimmed.contains(".")) {
            normalized = trimmed.replace(',', '.');
        }
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException ex) {
            throw InvalidFormatException.from(parser, "gpa must be numeric", raw, BigDecimal.class);
        }
    }
}

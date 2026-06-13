package com.project.project.dto.auth.deserializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

/**
 * Accepts integer values from number or numeric string.
 */
public class LenientIntegerDeserializer extends StdDeserializer<Integer> {

    public LenientIntegerDeserializer() {
        super(Integer.class);
    }

    @Override
    public Integer deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonToken token = parser.currentToken();
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        if (token == JsonToken.VALUE_NUMBER_INT) {
            return parser.getIntValue();
        }
        if (token == JsonToken.VALUE_NUMBER_FLOAT) {
            throw InvalidFormatException.from(parser, "classYear must be an integer", parser.getText(), Integer.class);
        }
        if (token != JsonToken.VALUE_STRING) {
            throw InvalidFormatException.from(parser, "classYear must be an integer", parser.getText(), Integer.class);
        }

        String raw = parser.getValueAsString();
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(trimmed);
        } catch (NumberFormatException ex) {
            throw InvalidFormatException.from(parser, "classYear must be an integer", raw, Integer.class);
        }
    }
}

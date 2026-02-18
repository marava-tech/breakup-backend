package com.breakupstories.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom deserializer for handling lists of names, tags, locations, themes, and cultural elements.
 * Supports various input formats:
 * - JSON array: ["item1", "item2", "item3"]
 * - Comma-separated string: "item1, item2, item3"
 * - Single string: "item1"
 * - Null or empty: returns empty list
 */
public class NamesListDeserializer extends JsonDeserializer<List<String>> {

    @Override
    public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        List<String> result = new ArrayList<>();

        JsonToken token = p.getCurrentToken();

        if (token == JsonToken.VALUE_NULL) {
            return result;
        }

        if (token == JsonToken.VALUE_STRING) {
            String value = p.getValueAsString();
            if (value == null || value.trim().isEmpty()) {
                return result;
            }

            // Handle comma-separated string
            String[] items = value.split(",");
            for (String item : items) {
                String trimmed = item.trim();
                if (!trimmed.isEmpty()) {
                    result.add(trimmed);
                }
            }
            return result;
        }

        if (token == JsonToken.START_ARRAY) {
            // Handle JSON array
            ObjectMapper mapper = (ObjectMapper) p.getCodec();
            JsonNode node = mapper.readTree(p);

            for (JsonNode element : node) {
                if (element.isTextual()) {
                    String value = element.asText().trim();
                    if (!value.isEmpty()) {
                        result.add(value);
                    }
                }
            }
            return result;
        }

        // For any other token type, try to get as string and parse
        String value = p.getValueAsString();
        if (value != null && !value.trim().isEmpty()) {
            result.add(value.trim());
        }

        return result;
    }
}

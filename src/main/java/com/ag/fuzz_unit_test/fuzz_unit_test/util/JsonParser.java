package com.ag.fuzz_unit_test.fuzz_unit_test.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * A utility class for parsing and processing JSON strings.
 * This class is intentionally vulnerable to certain edge cases
 * to demonstrate the effectiveness of fuzz testing.
 */
@Component
public class JsonParser {

    private final ObjectMapper objectMapper;

    public JsonParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Parses a JSON string into a JsonNode.
     *
     * @param json The JSON string to parse
     * @return A JsonNode representing the parsed JSON
     * @throws JsonProcessingException If the input is not valid JSON
     */
    public JsonNode parse(String json) throws JsonProcessingException {
        // Deliberately vulnerable implementation:
        // 1. No input size validation (could lead to OOM)
        // 2. No depth validation (could lead to stack overflow)
        return objectMapper.readTree(json);
    }

    /**
     * Extracts a value from a JSON node by path.
     * This method is intentionally vulnerable to certain paths.
     *
     * @param node The JsonNode to extract from
     * @param path The path to the value (format: "field1.field2[0].field3")
     * @return The extracted value as a String, or null if not found
     */
    public String extractValue(JsonNode node, String path) {
        if (node == null || path == null || path.isEmpty()) {
            return null;
        }

        // Deliberate bug: not validating path structure properly
        String[] parts = path.split("\\.");
        JsonNode current = node;

        for (String part : parts) {
            // Handle array access, e.g., "field[0]"
            if (part.contains("[") && part.endsWith("]")) {
                int bracketIndex = part.indexOf('[');
                String fieldName = part.substring(0, bracketIndex);

                // Intentional bug: not validating that the index is a number
                // This could cause NumberFormatException
                String indexStr = part.substring(bracketIndex + 1, part.length() - 1);
                int index = Integer.parseInt(indexStr);

                current = current.get(fieldName);
                if (current == null || !current.isArray()) {
                    return null;
                }

                // Intentional bug: not checking array bounds
                // This could cause ArrayIndexOutOfBoundsException
                current = current.get(index);
            } else {
                current = current.get(part);
            }

            if (current == null) {
                return null;
            }
        }

        return current.asText();
    }
}

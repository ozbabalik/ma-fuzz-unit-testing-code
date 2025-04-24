package com.ag.fuzz_unit_test.fuzz_unit_test.fuzz;

import com.ag.fuzz_unit_test.fuzz_unit_test.util.JsonParser;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.UserRepository;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.OrderRepository;

@SpringBootTest
public class JsonParserFuzzTest {

    @MockBean
    private UserRepository userRepository;
    
    @MockBean
    private OrderRepository orderRepository;

    @Autowired
    private JsonParser jsonParser;

    @BeforeEach
    void setUp() {
        // Any setup can be done here
    }

    @FuzzTest
    void testJsonParsing(FuzzedDataProvider data) {
        // Generate a random string that might be valid or invalid JSON
        String jsonInput = data.consumeRemainingAsString();

        try {
            // Try to parse the input as JSON
            JsonNode node = jsonParser.parse(jsonInput);

            // If parsing succeeded, verify some properties
            if (node != null) {
                // Test further operations on the parsed JSON
                if (node.isObject()) {
                    // Extract all field names
                    node.fieldNames().forEachRemaining(fieldName -> {
                        // Just access each field to ensure they can be processed
                        JsonNode fieldValue = node.get(fieldName);
                    });
                } else if (node.isArray()) {
                    // Process each element in the array
                    for (int i = 0; i < node.size(); i++) {
                        JsonNode element = node.get(i);
                    }
                }
            }
        } catch (JsonProcessingException e) {
            // Expected exception for invalid JSON, no need to propagate
        } catch (StackOverflowError e) {
            // This could happen with deeply nested JSON - a potential DoS vulnerability
            throw new AssertionError("Stack overflow detected with input: " +
                                     (jsonInput.length() > 100 ? jsonInput.substring(0, 100) + "..."
                                             : jsonInput));
        } catch (OutOfMemoryError e) {
            // This could happen with maliciously crafted JSON - a potential DoS vulnerability
            throw new AssertionError("Out of memory detected with input: " +
                                     (jsonInput.length() > 100 ? jsonInput.substring(0, 100) + "..."
                                             : jsonInput));
        }
    }

    @FuzzTest
    void testJsonPathExtraction(FuzzedDataProvider data) {
        // Generate a small, valid JSON object for testing
        String jsonInput = "{\"name\":\"John\",\"age\":30,\"nested\":{\"field\":\"value\"},\"array\":[1,2,3]}";

        try {
            // Parse the base JSON input (which is valid)
            JsonNode node = jsonParser.parse(jsonInput);

            // Generate a random JSON path to test extraction
            String path = data.consumeRemainingAsString();

            try {
                // Try to extract using the fuzzed path
                String result = jsonParser.extractValue(node, path);
                // No need to assert anything particular about the result
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                // These are expected exceptions for certain paths
            } catch (Exception e) {
                // Other exceptions might indicate bugs
                throw new AssertionError("Unexpected exception: " + e.getMessage(), e);
            }
        } catch (JsonProcessingException e) {
            // This shouldn't happen with our fixed valid JSON
            throw new AssertionError("Error parsing our fixed valid JSON: " + e.getMessage(), e);
        }
    }
}

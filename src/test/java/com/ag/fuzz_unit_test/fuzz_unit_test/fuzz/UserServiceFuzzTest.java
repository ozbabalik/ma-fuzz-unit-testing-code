package com.ag.fuzz_unit_test.fuzz_unit_test.fuzz;

import com.ag.fuzz_unit_test.fuzz_unit_test.service.UserService;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = FuzzTestConfig.class)
public class UserServiceFuzzTest {

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        // Any setup can be done here
    }

    @FuzzTest
    void testEmailValidation(FuzzedDataProvider data) {
        // Generate a random email string with various characteristics
        String email = data.consumeRemainingAsString();

        try {
            userService.validateEmail(email);

            // If we get here, the email was considered valid
            // Let's perform some basic assertions to ensure it's actually valid
            assert email.contains("@") : "Valid email should contain @";

            String[] parts = email.split("@");
            assert parts.length == 2 : "Email should have exactly one @ symbol";
            assert !parts[0].isEmpty() : "Local part should not be empty";
            assert !parts[1].isEmpty() : "Domain part should not be empty";
            assert parts[1].contains(".") : "Domain should contain a dot";

        } catch (IllegalArgumentException e) {
            // This is expected for invalid emails
            // No need to do anything special
        } catch (Exception e) {
            // Other exceptions are not expected
            throw new AssertionError("Unexpected exception: " + e.getMessage(), e);
        }
    }
}

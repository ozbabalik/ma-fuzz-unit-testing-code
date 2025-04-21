package com.ag.fuzz_unit_test.fuzz_unit_test.fuzz.generators;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;

import java.util.stream.Stream;

public class EmailGenerator implements ArgumentsProvider, AnnotationConsumer<FuzzTest> {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        // This method will generate a variety of different email formats
        return Stream.of(
            Arguments.of("user@example.com"),                       // standard valid email
            Arguments.of(""), 
            Arguments.of("@example.com"),                           // missing username
            Arguments.of("user@"),                                  // missing domain
            Arguments.of("user@domain"),                            // missing TLD
            Arguments.of("user.example.com"),                       // missing @
            Arguments.of("user@exam ple.com"),                      // spaces in domain
            Arguments.of("us er@example.com"),                      // spaces in username
            Arguments.of("user@exam\nple.com"),                     // newline in domain
            Arguments.of("very.long." + "x".repeat(200) + "@example.com"), // very long email
            Arguments.of("user@" + "x".repeat(200) + ".com"),        // very long domain
            Arguments.of("user+tag@example.com"),                   // valid + in email
            Arguments.of("<script>alert(1)</script>@example.com"),  // XSS attempt in username
            Arguments.of("user@example.com<script>alert(1)</script>") // XSS attempt at end
        );
    }

    @Override
    public void accept(FuzzTest fuzzTest) {
        // No configuration needed from the annotation
    }
} 
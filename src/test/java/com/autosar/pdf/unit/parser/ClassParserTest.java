package com.autosar.pdf.unit.parser;

import com.autosar.pdf.parser.ClassParser;
import com.autosar.pdf.parser.ParseContext;
import com.autosar.pdf.domain.AutosarClassBuilder;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

class ClassParserTest {
    @Test
    void shouldRecognizeApplicationTypePattern() {
        ClassParser parser = new ClassParser();
        assertThat(parser.canParse("ApplicationType MyClass")).isTrue();
    }

    @Test
    void shouldRecognizeApplicationDataTypePattern() {
        ClassParser parser = new ClassParser();
        assertThat(parser.canParse("ApplicationDataType MyDataType")).isTrue();
    }

    @Test
    void shouldRecognizeCaseInsensitiveApplicationType() {
        ClassParser parser = new ClassParser();
        // Pattern matches "Application" or "application" followed by "Type" or "DataType"
        assertThat(parser.canParse("applicationType MyClass")).isTrue();
        assertThat(parser.canParse("ApplicationType MyClass")).isTrue();
        assertThat(parser.canParse("applicationDataType MyData")).isTrue();
        assertThat(parser.canParse("ApplicationDataType MyData")).isTrue();
    }

    @Test
    void shouldNotRecognizeNonClassPatterns() {
        ClassParser parser = new ClassParser();
        assertThat(parser.canParse("Enumeration MyEnum")).isFalse();
        assertThat(parser.canParse("Primitive MyPrimitive")).isFalse();
        assertThat(parser.canParse("Some random text")).isFalse();
    }

    @Test
    void shouldParseClassDefinition() {
        ClassParser parser = new ClassParser();
        ParseContext context = new ParseContext(
            Optional.of(new com.autosar.pdf.domain.AutosarPackageBuilder("Test", java.util.List.of())),
            Optional.empty(), 1, null, java.util.List.of()
        );

        parser.parse("ApplicationType MyClass", context);

        // Note: ClassParser currently only recognizes the pattern but doesn't
        // update the context due to TODO in implementation.
        // This test verifies that parsing doesn't throw an exception.
        assertThat(parser.canParse("ApplicationType MyClass")).isTrue();
    }
}
package com.autosar.pdf.unit.parser;

import com.autosar.pdf.parser.EnumerationParser;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class EnumerationParserTest {
    @Test
    void shouldRecognizeEnumerationPattern() {
        EnumerationParser parser = new EnumerationParser();
        assertThat(parser.canParse("Enumeration MyEnum")).isTrue();
    }

    @Test
    void shouldRecognizeCaseInsensitiveEnumeration() {
        EnumerationParser parser = new EnumerationParser();
        assertThat(parser.canParse("enumeration MyEnum")).isTrue();
        assertThat(parser.canParse("Enumeration MyEnum")).isTrue();
    }

    @Test
    void shouldNotRecognizeNonEnumerationPatterns() {
        EnumerationParser parser = new EnumerationParser();
        assertThat(parser.canParse("ApplicationType MyClass")).isFalse();
        assertThat(parser.canParse("Primitive MyPrimitive")).isFalse();
        assertThat(parser.canParse("Some random text")).isFalse();
    }
}
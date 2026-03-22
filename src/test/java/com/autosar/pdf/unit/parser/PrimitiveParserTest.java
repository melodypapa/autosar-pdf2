package com.autosar.pdf.unit.parser;

import com.autosar.pdf.parser.PrimitiveParser;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class PrimitiveParserTest {
    @Test
    void shouldRecognizePrimitivePattern() {
        PrimitiveParser parser = new PrimitiveParser();
        assertThat(parser.canParse("Primitive MyPrimitive")).isTrue();
    }

    @Test
    void shouldRecognizeCaseInsensitivePrimitive() {
        PrimitiveParser parser = new PrimitiveParser();
        assertThat(parser.canParse("primitive MyPrimitive")).isTrue();
        assertThat(parser.canParse("Primitive MyPrimitive")).isTrue();
    }

    @Test
    void shouldNotRecognizeNonPrimitivePatterns() {
        PrimitiveParser parser = new PrimitiveParser();
        assertThat(parser.canParse("ApplicationType MyClass")).isFalse();
        assertThat(parser.canParse("Enumeration MyEnum")).isFalse();
        assertThat(parser.canParse("Some random text")).isFalse();
    }
}
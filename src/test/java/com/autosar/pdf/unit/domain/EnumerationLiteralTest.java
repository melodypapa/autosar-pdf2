package com.autosar.pdf.unit.domain;

import com.autosar.pdf.domain.EnumerationLiteral;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

class EnumerationLiteralTest {
    @Test
    void shouldCreateEnumerationLiteralWithAllFields() {
        EnumerationLiteral literal = new EnumerationLiteral(
            "FREEZE", Optional.of("0"), Optional.of("Event debounce counter frozen")
        );
        assertThat(literal.name()).isEqualTo("FREEZE");
        assertThat(literal.value()).hasValue("0");
        assertThat(literal.description()).hasValue("Event debounce counter frozen");
    }

    @Test
    void shouldCreateEnumerationLiteralWithOptionalFields() {
        EnumerationLiteral literal = new EnumerationLiteral(
            "name", Optional.empty(), Optional.empty()
        );
        assertThat(literal.value()).isEmpty();
        assertThat(literal.description()).isEmpty();
    }
}

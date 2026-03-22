package com.autosar.pdf.unit.domain;

import com.autosar.pdf.domain.Attribute;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

class AttributeTest {
    @Test
    void shouldCreateAttributeWithAllFields() {
        Attribute attr = new Attribute(
            "shortName", "Identifier", Optional.of("defaultName"), Optional.of("1")
        );
        assertThat(attr.name()).isEqualTo("shortName");
        assertThat(attr.type()).isEqualTo("Identifier");
        assertThat(attr.defaultValue()).hasValue("defaultName");
        assertThat(attr.multiplicity()).hasValue("1");
    }

    @Test
    void shouldCreateAttributeWithOptionalFields() {
        Attribute attr = new Attribute(
            "name", "String", Optional.empty(), Optional.empty()
        );
        assertThat(attr.defaultValue()).isEmpty();
        assertThat(attr.multiplicity()).isEmpty();
    }
}
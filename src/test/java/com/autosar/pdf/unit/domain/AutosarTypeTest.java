package com.autosar.pdf.unit.domain;

import com.autosar.pdf.domain.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

class AutosarTypeTest {
    @Test
    void shouldCreateAutosarClass() {
        Attribute attr = new Attribute("name", "String", Optional.empty(), Optional.of("1"));
        AutosarClass cls = new AutosarClass(
            "MyClass", false, "ApplicationType",
            List.of(attr), List.of("BaseClass"), Optional.empty(), Optional.empty(), List.of()
        );
        assertThat(cls.name()).isEqualTo("MyClass");
        assertThat(cls.isAbstract()).isFalse();
        assertThat(cls.atpType()).isEqualTo("ApplicationType");
        assertThat(cls.attributes()).hasSize(1);
        assertThat(cls.bases()).containsExactly("BaseClass");
    }

    @Test
    void shouldCreateAutosarEnumeration() {
        EnumerationLiteral literal = new EnumerationLiteral("VAL1", Optional.of("1"), Optional.of("Value 1"));
        AutosarEnumeration enum_ = new AutosarEnumeration(
            "MyEnum", List.of(literal)
        );
        assertThat(enum_.name()).isEqualTo("MyEnum");
        assertThat(enum_.literals()).hasSize(1);
    }

    @Test
    void shouldCreateAutosarPrimitive() {
        Attribute attr = new Attribute("value", "String", Optional.empty(), Optional.of("1"));
        AutosarPrimitive prim = new AutosarPrimitive(
            "MyPrimitive", List.of(attr)
        );
        assertThat(prim.name()).isEqualTo("MyPrimitive");
        assertThat(prim.attributes()).hasSize(1);
    }
}
package com.autosar.pdf.unit.domain;

import com.autosar.pdf.domain.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class AutosarDocTest {
    @Test
    void shouldCreateAutosarDoc() {
        DocumentSource source = new DocumentSource("test.pdf", 1, Optional.empty(), Optional.empty());
        Attribute attr = new Attribute("name", "String", Optional.empty(), Optional.of("1"));
        AutosarClass cls = new AutosarClass(
            "MyClass", false, "ApplicationType",
            List.of(attr), List.of(), Optional.empty(), Optional.empty(), List.of()
        );

        AutosarPackage pkg = new AutosarPackage(
            "MyPackage", List.of("M2", "AUTOSAR"),
            Map.of("MyClass", cls), source
        );

        AutosarDoc doc = new AutosarDoc(List.of(pkg));

        assertThat(doc.packages()).hasSize(1);
        assertThat(doc.packages().get(0)).isEqualTo(pkg);
    }
}
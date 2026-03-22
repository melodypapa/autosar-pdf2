package com.autosar.pdf.unit.domain;

import com.autosar.pdf.domain.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class AutosarPackageTest {
    @Test
    void shouldCreateAutosarPackage() {
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

        assertThat(pkg.name()).isEqualTo("MyPackage");
        assertThat(pkg.path()).containsExactly("M2", "AUTOSAR");
        assertThat(pkg.types()).hasSize(1);
        assertThat(pkg.source()).isEqualTo(source);
    }

    @Test
    void shouldBuildAutosarPackage() {
        AutosarPackageBuilder builder = new AutosarPackageBuilder(
            "MyPackage", List.of("M2", "AUTOSAR")
        );

        DocumentSource source = new DocumentSource("test.pdf", 1, Optional.empty(), Optional.empty());
        Attribute attr = new Attribute("name", "String", Optional.empty(), Optional.of("1"));
        AutosarClass cls = new AutosarClass(
            "MyClass", false, "ApplicationType",
            List.of(attr), List.of(), Optional.empty(), Optional.empty(), List.of()
        );

        builder.addType(cls);
        builder.setSource(source);

        AutosarPackage pkg = builder.build();

        assertThat(pkg.name()).isEqualTo("MyPackage");
        assertThat(pkg.types()).hasSize(1);
        assertThat(pkg.types().get("MyClass")).isEqualTo(cls);
    }
}
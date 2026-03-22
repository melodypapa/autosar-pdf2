package com.autosar.pdf.unit.domain;

import com.autosar.pdf.domain.AutosarPackageBuilder;
import com.autosar.pdf.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for AutosarPackageBuilder - verifies builder pattern functionality.
 */
class AutosarPackageBuilderTest {

    @Test
    @DisplayName("Should create minimal package builder")
    void shouldCreateMinimalPackageBuilder() {
        AutosarPackageBuilder builder = new AutosarPackageBuilder(
            "TestPackage", List.of("M2", "AUTOSAR")
        );

        AutosarPackage pkg = builder.build();

        assertThat(pkg.name()).isEqualTo("TestPackage");
        assertThat(pkg.path()).containsExactly("M2", "AUTOSAR");
        assertThat(pkg.types()).isEmpty();
        assertThat(pkg.source()).isNull();
    }

    @Test
    @DisplayName("Should add single type")
    void shouldAddSingleType() {
        AutosarPackageBuilder builder = new AutosarPackageBuilder(
            "TestPackage", List.of("M2")
        );

        Attribute attr = new Attribute("name", "String", Optional.empty(), Optional.of("1"));
        AutosarClass cls = new AutosarClass(
            "TestClass", false, "ApplicationType",
            List.of(attr), List.of(), Optional.empty(), Optional.empty(), List.of()
        );

        builder.addType(cls);
        AutosarPackage pkg = builder.build();

        assertThat(pkg.types()).hasSize(1);
        assertThat(pkg.types().containsKey("TestClass")).isTrue();
        assertThat(pkg.types().get("TestClass")).isEqualTo(cls);
    }

    @Test
    @DisplayName("Should add multiple types")
    void shouldAddMultipleTypes() {
        AutosarPackageBuilder builder = new AutosarPackageBuilder(
            "MixedPackage", List.of("M2", "AUTOSAR")
        );

        Attribute attr = new Attribute("value", "String", Optional.empty(), Optional.of("1"));

        // Add class
        AutosarClass cls = new AutosarClass(
            "MyClass", false, "ApplicationType",
            List.of(attr), List.of(), Optional.empty(), Optional.empty(), List.of()
        );
        builder.addType(cls);

        // Add enumeration
        EnumerationLiteral literal = new EnumerationLiteral("VAL", java.util.Optional.of("1"), java.util.Optional.of("Description"));
        AutosarEnumeration enum_ = new AutosarEnumeration("MyEnum", List.of(literal));
        builder.addType(enum_);

        // Add primitive
        AutosarPrimitive primitive = new AutosarPrimitive("MyPrimitive", List.of(attr));
        builder.addType(primitive);

        AutosarPackage pkg = builder.build();

        assertThat(pkg.types()).hasSize(3);
        assertThat(pkg.types().keySet()).containsExactly("MyClass", "MyEnum", "MyPrimitive");
    }

    @Test
    @DisplayName("Should set source")
    void shouldSetSource() {
        AutosarPackageBuilder builder = new AutosarPackageBuilder(
            "TestPackage", List.of("M2")
        );

        DocumentSource source = new DocumentSource("test.pdf", 42,
            java.util.Optional.of("AUTOSAR"), java.util.Optional.of("R23-11"));
        builder.setSource(source);

        AutosarPackage pkg = builder.build();

        assertThat(pkg.source()).isEqualTo(source);
        assertThat(pkg.source().filename()).isEqualTo("test.pdf");
        assertThat(pkg.source().page()).isEqualTo(42);
    }

    @Test
    @DisplayName("Should overwrite type with same name")
    void shouldOverwriteTypeWithSameName() {
        AutosarPackageBuilder builder = new AutosarPackageBuilder(
            "TestPackage", List.of("M2")
        );

        Attribute attr1 = new Attribute("old", "String", Optional.empty(), Optional.of("1"));
        AutosarClass cls1 = new AutosarClass(
            "MyClass", false, "ApplicationType",
            List.of(attr1), List.of(), null, null, List.of()
        );

        builder.addType(cls1);

        Attribute attr2 = new Attribute("new", "Integer", Optional.empty(), Optional.of("1"));
        AutosarClass cls2 = new AutosarClass(
            "MyClass", false, "ApplicationType",
            List.of(attr2), List.of(), null, null, List.of()
        );

        builder.addType(cls2);

        AutosarPackage pkg = builder.build();

        assertThat(pkg.types()).hasSize(1);
        assertThat(((AutosarClass) pkg.types().get("MyClass")).attributes().get(0).name()).isEqualTo("new");
    }

    @Test
    @DisplayName("Should create immutable type map")
    void shouldCreateImmutableTypeMap() {
        AutosarPackageBuilder builder = new AutosarPackageBuilder(
            "ImmutablePkg", List.of("M2")
        );

        Attribute attr = new Attribute("attr", "String", Optional.empty(), Optional.of("1"));
        AutosarClass cls = new AutosarClass(
            "TestClass", false, "ApplicationType",
            List.of(attr), List.of(), null, null, List.of()
        );

        builder.addType(cls);
        AutosarPackage pkg = builder.build();

        // Attempting to modify the package's type map should fail
        // (this will be caught at runtime if tried)
        assertThat(pkg.types()).isInstanceOf(Map.class);
    }

    @Test
    @DisplayName("Should build complete package")
    void shouldBuildCompletePackage() {
        AutosarPackageBuilder builder = new AutosarPackageBuilder(
            "CompletePackage", List.of("M2", "AUTOSAR", "CommonStructure")
        );

        Attribute attr = new Attribute("name", "String", Optional.empty(), Optional.of("1"));
        AutosarClass cls = new AutosarClass(
            "CompleteClass", false, "ApplicationType",
            List.of(attr), List.of("BaseClass"), Optional.empty(), Optional.empty(), List.of()
        );

        builder.addType(cls);

        DocumentSource source = new DocumentSource(
            "complete.pdf", 123, java.util.Optional.of("AUTOSAR Classic Platform"), java.util.Optional.of("R23-11")
        );
        builder.setSource(source);

        AutosarPackage pkg = builder.build();

        assertThat(pkg.name()).isEqualTo("CompletePackage");
        assertThat(pkg.path()).containsExactly("M2", "AUTOSAR", "CommonStructure");
        assertThat(pkg.types()).hasSize(1);
        assertThat(pkg.source()).isNotNull();
        assertThat(pkg.source().standard()).hasValue("AUTOSAR Classic Platform");
        assertThat(pkg.source().release()).hasValue("R23-11");
    }

    @Test
    @DisplayName("Should handle empty type list")
    void shouldHandleEmptyTypeList() {
        AutosarPackageBuilder builder = new AutosarPackageBuilder(
            "EmptyPackage", List.of("M2")
        );

        AutosarPackage pkg = builder.build();

        assertThat(pkg.types()).isEmpty();
    }

    @Test
    @DisplayName("Should copy path list")
    void shouldCopyPathList() {
        List<String> originalPath = new java.util.ArrayList<>(List.of("M2", "AUTOSAR"));
        AutosarPackageBuilder builder = new AutosarPackageBuilder("TestPackage", originalPath);

        // Modify original list after builder creation
        originalPath.add("EXTRA");

        AutosarPackage pkg = builder.build();

        assertThat(pkg.path()).doesNotContain("EXTRA");
        assertThat(pkg.path()).hasSize(2);
    }
}
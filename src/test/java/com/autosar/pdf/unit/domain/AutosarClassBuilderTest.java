package com.autosar.pdf.unit.domain;

import com.autosar.pdf.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for AutosarClassBuilder - verifies builder pattern functionality.
 */
class AutosarClassBuilderTest {

    @Test
    @DisplayName("Should create minimal class builder")
    void shouldCreateMinimalClassBuilder() {
        AutosarClassBuilder builder = new AutosarClassBuilder("TestClass", "ApplicationType");
        AutosarClass cls = builder.build();

        assertThat(cls.name()).isEqualTo("TestClass");
        assertThat(cls.atpType()).isEqualTo("ApplicationType");
        assertThat(cls.isAbstract()).isFalse();
    }

    @Test
    @DisplayName("Should build abstract class")
    void shouldBuildAbstractClass() {
        AutosarClassBuilder builder = new AutosarClassBuilder(
            "AbstractClass", "ApplicationType"
        ).isAbstract(true);

        AutosarClass cls = builder.build();
        assertThat(cls.isAbstract()).isTrue();
    }

    @Test
    @DisplayName("Should add multiple attributes")
    void shouldAddMultipleAttributes() {
        AutosarClassBuilder builder = new AutosarClassBuilder(
            "ClassWithAttrs", "ApplicationType"
        );

        builder.addAttribute(new Attribute("attr1", "String", Optional.empty(), Optional.of("1")));
        builder.addAttribute(new Attribute("attr2", "Integer", Optional.empty(), Optional.of("0..*")));
        builder.addAttribute(new Attribute("attr3", "Boolean", Optional.of("true"), Optional.of("1")));

        AutosarClass cls = builder.build();

        assertThat(cls.attributes()).hasSize(3);
        assertThat(cls.attributes().get(0).name()).isEqualTo("attr1");
        assertThat(cls.attributes().get(2).defaultValue()).hasValue("true");
    }

    @Test
    @DisplayName("Should add multiple base classes")
    void shouldAddMultipleBaseClasses() {
        AutosarClassBuilder builder = new AutosarClassBuilder(
            "MultiBaseClass", "ApplicationType"
        );

        builder.addBase("Base1");
        builder.addBase("Base2");
        builder.addBase("Base3");

        AutosarClass cls = builder.build();

        assertThat(cls.bases()).containsExactly("Base1", "Base2", "Base3");
    }

    @Test
    @DisplayName("Should set parent class")
    void shouldSetParentClass() {
        AutosarClassBuilder builder = new AutosarClassBuilder(
            "ChildClass", "ApplicationType"
        );

        AutosarClass parent = new AutosarClass(
            "ParentClass", false, "ApplicationType",
            List.of(), List.of(), null, null, List.of()
        );

        builder.parent(parent);
        AutosarClass cls = builder.build();

        assertThat(cls.parent()).hasValue(parent);
        assertThat(cls.parent().get().name()).isEqualTo("ParentClass");
    }

    @Test
    @DisplayName("Should set aggregated by package")
    void shouldSetAggregatedByPackage() {
        AutosarClassBuilder builder = new AutosarClassBuilder(
            "AggregatedClass", "ApplicationType"
        );

        builder.aggregatedBy("AggregatingPackage");
        AutosarClass cls = builder.build();

        assertThat(cls.aggregatedBy()).hasValue("AggregatingPackage");
    }

    @Test
    @DisplayName("Should add multiple subclasses")
    void shouldAddMultipleSubclasses() {
        AutosarClassBuilder builder = new AutosarClassBuilder(
            "ParentClass", "ApplicationType"
        );

        builder.addSubclass("Child1");
        builder.addSubclass("Child2");

        AutosarClass cls = builder.build();

        assertThat(cls.subclasses()).containsExactly("Child1", "Child2");
    }

    @Test
    @DisplayName("Should support fluent API")
    void shouldSupportFluentApi() {
        AutosarClass cls = new AutosarClassBuilder("FluentClass", "ApplicationType")
            .isAbstract(true)
            .addBase("Base1")
            .addBase("Base2")
            .aggregatedBy("SomePackage")
            .addSubclass("Sub1")
            .build();

        assertThat(cls.name()).isEqualTo("FluentClass");
        assertThat(cls.isAbstract()).isTrue();
        assertThat(cls.bases()).hasSize(2);
        assertThat(cls.aggregatedBy()).hasValue("SomePackage");
        assertThat(cls.subclasses()).hasSize(1);
    }

    @Test
    @DisplayName("Should create immutable list copies")
    void shouldCreateImmutableListCopies() {
        AutosarClassBuilder builder = new AutosarClassBuilder(
            "ImmutableTest", "ApplicationType"
        );

        builder.addAttribute(new Attribute("attr1", "String", Optional.empty(), Optional.of("1")));
        builder.addBase("Base1");

        AutosarClass cls = builder.build();

        // Modifying builder after build should not affect the built class
        builder.addAttribute(new Attribute("attr2", "Integer", Optional.empty(), Optional.of("1")));
        builder.addBase("Base2");

        assertThat(cls.attributes()).hasSize(1);
        assertThat(cls.bases()).hasSize(1);
    }

    @Test
    @DisplayName("Should handle ApplicationDataType")
    void shouldHandleApplicationDataType() {
        AutosarClassBuilder builder = new AutosarClassBuilder(
            "MyDataType", "ApplicationDataType"
        );

        AutosarClass cls = builder.build();

        assertThat(cls.name()).isEqualTo("MyDataType");
        assertThat(cls.atpType()).isEqualTo("ApplicationDataType");
    }

    @Test
    @DisplayName("Should build class with all fields populated")
    void shouldBuildClassWithAllFieldsPopulated() {
        AutosarClassBuilder builder = new AutosarClassBuilder(
            "CompleteClass", "ApplicationType"
        );

        builder.isAbstract(true);
        builder.addAttribute(new Attribute("attr", "String", Optional.of("default"), Optional.of("1")));
        builder.addBase("BaseClass");
        builder.aggregatedBy("SomePackage");
        builder.addSubclass("SubClass1");
        builder.addSubclass("SubClass2");

        AutosarClass cls = builder.build();

        assertThat(cls.name()).isEqualTo("CompleteClass");
        assertThat(cls.isAbstract()).isTrue();
        assertThat(cls.attributes()).hasSize(1);
        assertThat(cls.bases()).containsExactly("BaseClass");
        assertThat(cls.aggregatedBy()).hasValue("SomePackage");
        assertThat(cls.subclasses()).containsExactly("SubClass1", "SubClass2");
    }
}
package com.autosar.pdf.integration;

import com.autosar.pdf.domain.*;
import com.autosar.pdf.extraction.PdfExtractor;
import com.autosar.pdf.extraction.TwoPhaseExtractor;
import com.autosar.pdf.parser.ParentResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for end-to-end PDF extraction and processing.
 *
 * These tests verify the complete pipeline from PDF input to structured output.
 */
class PdfIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Should extract basic document structure")
    void shouldExtractBasicDocumentStructure() {
        DocumentSource source = new DocumentSource("test.pdf", 1, Optional.of("AUTOSAR"), Optional.of("R23-11"));
        Attribute attr = new Attribute("shortName", "Identifier", Optional.empty(), Optional.of("1"));
        AutosarClass cls = new AutosarClass(
            "TestClass", false, "ApplicationType",
            List.of(attr), List.of("BaseClass"), Optional.empty(), Optional.empty(), List.of()
        );

        AutosarPackage pkg = new AutosarPackage(
            "TestPackage", List.of("M2", "AUTOSAR"),
            Map.of("TestClass", cls), source
        );

        AutosarDoc doc = new AutosarDoc(List.of(pkg));

        assertThat(doc.packages()).hasSize(1);
        assertThat(doc.packages().get(0).name()).isEqualTo("TestPackage");
    }

    @Test
    @DisplayName("Should resolve parent-child relationships")
    void shouldResolveParentChildRelationships() {
        // Create parent class
        AutosarClass parent = new AutosarClass(
            "BaseClass", false, "ApplicationType",
            List.of(), List.of(), null, null, List.of()
        );

        // Create child class
        AutosarClass child = new AutosarClass(
            "DerivedClass", false, "ApplicationType",
            List.of(), List.of("BaseClass"), null, null, List.of()
        );

        DocumentSource source = new DocumentSource("test.pdf", 1, Optional.empty(), Optional.empty());
        AutosarPackage pkg = new AutosarPackage(
            "TestPackage", List.of("M2"),
            Map.of("BaseClass", parent, "DerivedClass", child), source
        );

        // Resolve relationships
        ParentResolver resolver = new ParentResolver();
        resolver.resolveParents(List.of(pkg));

        // Verify resolution
        assertThat(resolver.hasClass("BaseClass")).isTrue();
        assertThat(resolver.hasClass("DerivedClass")).isTrue();
        assertThat(resolver.getChildren("BaseClass")).hasSize(1);
        assertThat(resolver.getChildren("BaseClass").get(0)).isEqualTo(child);
    }

    @Test
    @DisplayName("Should handle multi-package documents")
    void shouldHandleMultiPackageDocuments() {
        DocumentSource source1 = new DocumentSource("test1.pdf", 1, Optional.of("AUTOSAR"), Optional.of("R23-11"));
        DocumentSource source2 = new DocumentSource("test2.pdf", 1, Optional.of("AUTOSAR"), Optional.of("R23-11"));

        AutosarClass cls1 = new AutosarClass(
            "Class1", false, "ApplicationType",
            List.of(), List.of(), null, null, List.of()
        );

        AutosarClass cls2 = new AutosarClass(
            "Class2", false, "ApplicationType",
            List.of(), List.of(), null, null, List.of()
        );

        AutosarPackage pkg1 = new AutosarPackage(
            "Package1", List.of("M2", "AUTOSAR"),
            Map.of("Class1", cls1), source1
        );

        AutosarPackage pkg2 = new AutosarPackage(
            "Package2", List.of("M2", "AUTOSAR"),
            Map.of("Class2", cls2), source2
        );

        AutosarDoc doc = new AutosarDoc(List.of(pkg1, pkg2));

        assertThat(doc.packages()).hasSize(2);
        assertThat(doc.packages().get(0).name()).isEqualTo("Package1");
        assertThat(doc.packages().get(1).name()).isEqualTo("Package2");
    }

    @Test
    @DisplayName("Should handle enumeration with literals")
    void shouldHandleEnumerationWithLiterals() {
        EnumerationLiteral literal1 = new EnumerationLiteral("TRUE", Optional.of("true"), Optional.of("Affirmative"));
        EnumerationLiteral literal2 = new EnumerationLiteral("FALSE", Optional.of("false"), Optional.of("Negative"));

        AutosarEnumeration enum_ = new AutosarEnumeration(
            "BooleanEnum", List.of(literal1, literal2)
        );

        assertThat(enum_.name()).isEqualTo("BooleanEnum");
        assertThat(enum_.literals()).hasSize(2);
        assertThat(enum_.literals().get(0).name()).isEqualTo("TRUE");
        assertThat(enum_.literals().get(0).value()).hasValue("true");
    }

    @Test
    @DisplayName("Should handle primitive types with attributes")
    void shouldHandlePrimitiveTypesWithAttributes() {
        Attribute attr1 = new Attribute("value", "String", Optional.empty(), Optional.of("1"));
        Attribute attr2 = new Attribute("length", "Integer", Optional.empty(), Optional.of("1"));

        AutosarPrimitive primitive = new AutosarPrimitive(
            "StringPrimitive", List.of(attr1, attr2)
        );

        assertThat(primitive.name()).isEqualTo("StringPrimitive");
        assertThat(primitive.attributes()).hasSize(2);
    }

    @Test
    @DisplayName("Should create AutosarPackageBuilder correctly")
    void shouldCreateAutosarPackageBuilderCorrectly() {
        AutosarPackageBuilder builder = new AutosarPackageBuilder(
            "NewPackage", List.of("M2", "AUTOSAR", "Custom")
        );

        Attribute attr = new Attribute("name", "String", Optional.empty(), Optional.of("1"));
        AutosarClass cls = new AutosarClass(
            "NewClass", false, "ApplicationType",
            List.of(attr), List.of(), null, null, List.of()
        );

        builder.addType(cls);

        DocumentSource source = new DocumentSource("new.pdf", 1, Optional.empty(), Optional.empty());
        builder.setSource(source);

        AutosarPackage pkg = builder.build();

        assertThat(pkg.name()).isEqualTo("NewPackage");
        assertThat(pkg.path()).containsExactly("M2", "AUTOSAR", "Custom");
        assertThat(pkg.types()).hasSize(1);
        assertThat(pkg.types().containsKey("NewClass")).isTrue();
    }

    @Test
    @DisplayName("Should handle AutosarClassBuilder correctly")
    void shouldHandleAutosarClassBuilderCorrectly() {
        AutosarClassBuilder builder = new AutosarClassBuilder("ComplexClass", "ApplicationType");

        builder.isAbstract(true);
        builder.addAttribute(new Attribute("attr1", "String", Optional.empty(), Optional.of("1")));
        builder.addAttribute(new Attribute("attr2", "Integer", Optional.empty(), Optional.of("0..*")));
        builder.addBase("BaseClass1");
        builder.addBase("BaseClass2");
        builder.aggregatedBy("AggregatePackage");
        builder.addSubclass("ChildClass");

        AutosarClass cls = builder.build();

        assertThat(cls.name()).isEqualTo("ComplexClass");
        assertThat(cls.isAbstract()).isTrue();
        assertThat(cls.atpType()).isEqualTo("ApplicationType");
        assertThat(cls.attributes()).hasSize(2);
        assertThat(cls.bases()).hasSize(2);
        assertThat(cls.aggregatedBy()).hasValue("AggregatePackage");
        assertThat(cls.subclasses()).hasSize(1);
    }

    @Test
    @DisplayName("Should handle cross-package references")
    void shouldHandleCrossPackageReferences() {
        DocumentSource source1 = new DocumentSource("pkg1.pdf", 1, Optional.empty(), Optional.empty());
        DocumentSource source2 = new DocumentSource("pkg2.pdf", 1, Optional.empty(), Optional.empty());

        // Parent in package 1
        AutosarClass parent = new AutosarClass(
            "SharedBase", false, "ApplicationType",
            List.of(), List.of(), null, null, List.of()
        );

        // Child in package 2 referencing parent in package 1
        AutosarClass child = new AutosarClass(
            "DerivedInPkg2", false, "ApplicationType",
            List.of(), List.of("SharedBase"), null, null, List.of()
        );

        AutosarPackage pkg1 = new AutosarPackage(
            "Package1", List.of("M2"),
            Map.of("SharedBase", parent), source1
        );

        AutosarPackage pkg2 = new AutosarPackage(
            "Package2", List.of("M2"),
            Map.of("DerivedInPkg2", child), source2
        );

        // Resolve - should find parent across packages
        ParentResolver resolver = new ParentResolver();
        resolver.resolveParents(List.of(pkg1, pkg2));

        assertThat(resolver.getChildren("SharedBase")).hasSize(1);
        assertThat(resolver.getChildren("SharedBase").get(0)).isEqualTo(child);
    }

    @Test
    @DisplayName("Should handle missing parent gracefully")
    void shouldHandleMissingParentGracefully() {
        AutosarClass orphan = new AutosarClass(
            "OrphanClass", false, "ApplicationType",
            List.of(), List.of("NonExistentParent"), Optional.empty(), Optional.empty(), List.of()
        );

        DocumentSource source = new DocumentSource("test.pdf", 1, Optional.empty(), Optional.empty());
        AutosarPackage pkg = new AutosarPackage(
            "TestPackage", List.of("M2"),
            Map.of("OrphanClass", orphan), source
        );

        ParentResolver resolver = new ParentResolver();
        assertThatCode(() -> resolver.resolveParents(List.of(pkg)))
            .doesNotThrowAnyException();

        // The orphan class should be tracked as having NonExistentParent as a base
        assertThat(resolver.getChildren("NonExistentParent")).containsExactly(orphan);
        // The orphan class itself should be in the class map
        assertThat(resolver.hasClass("OrphanClass")).isTrue();
    }

    @Test
    @DisplayName("Should merge multi-line attribute names")
    void shouldMergeMultiLineAttributeNames() {
        com.autosar.pdf.parser.AttributeMultiLineHandler handler =
            new com.autosar.pdf.parser.AttributeMultiLineHandler();

        // Hyphenated break
        assertThat(handler.mergeAttributeNames("re-", "quest2Support"))
            .isEqualTo("request2Support");

        // CamelCase fragment
        assertThat(handler.mergeAttributeNames("bswModule", "Documentation"))
            .isEqualTo("bswModuleDocumentation");

        // Multiple hyphens
        assertThat(handler.mergeAttributeNames("pre-", "fix-", "ed"))
            .isEqualTo("prefixed");
    }

    @Test
    @DisplayName("Should extract with TwoPhaseExtractor skeleton")
    void shouldExtractWithTwoPhaseExtractorSkeleton() {
        PdfExtractor extractor = new TwoPhaseExtractor();

        // This will create an empty document since actual extraction is not implemented
        AutosarDoc doc = extractor.extract("examples/pdf/AUTOSAR_FO_TPS_GenericStructureTemplate.pdf");

        assertThat(doc).isNotNull();
        assertThat(doc.packages()).isNotNull();
    }

    @Test
    @DisplayName("Should verify AutosarType sealed interface")
    void shouldVerifyAutosarTypeSealedInterface() {
        // Create instances of all implementing types
        Attribute attr = new Attribute("name", "String", Optional.empty(), Optional.of("1"));

        AutosarClass cls = new AutosarClass(
            "MyClass", false, "ApplicationType",
            List.of(attr), List.of(), null, null, List.of()
        );

        EnumerationLiteral literal = new EnumerationLiteral("VAL", Optional.of("1"), Optional.of("Description"));
        AutosarEnumeration enum_ = new AutosarEnumeration("MyEnum", List.of(literal));

        AutosarPrimitive primitive = new AutosarPrimitive("MyPrimitive", List.of(attr));

        // All should be instances of AutosarType
        assertThat(cls).isInstanceOf(AutosarType.class);
        assertThat(enum_).isInstanceOf(AutosarType.class);
        assertThat(primitive).isInstanceOf(AutosarType.class);

        // All should have names
        assertThat(cls.name()).isEqualTo("MyClass");
        assertThat(enum_.name()).isEqualTo("MyEnum");
        assertThat(primitive.name()).isEqualTo("MyPrimitive");
    }

    @Test
    @DisplayName("Should create complex AutosarPackage with multiple types")
    void shouldCreateComplexAutosarPackageWithMultipleTypes() {
        DocumentSource source = new DocumentSource("complex.pdf", 1, Optional.of("AUTOSAR"), Optional.of("R23-11"));

        // Create multiple types
        Attribute attr = new Attribute("name", "String", Optional.empty(), Optional.of("1"));

        AutosarClass cls = new AutosarClass(
            "ComplexClass", false, "ApplicationType",
            List.of(attr), List.of(), null, null, List.of()
        );

        EnumerationLiteral literal = new EnumerationLiteral("VAL", Optional.of("1"), Optional.of("Description"));
        AutosarEnumeration enum_ = new AutosarEnumeration("ComplexEnum", List.of(literal));

        AutosarPrimitive primitive = new AutosarPrimitive("ComplexPrimitive", List.of(attr));

        Map<String, AutosarType> types = Map.of(
            "ComplexClass", cls,
            "ComplexEnum", enum_,
            "ComplexPrimitive", primitive
        );

        AutosarPackage pkg = new AutosarPackage(
            "ComplexPackage",
            List.of("M2", "AUTOSAR", "Complex"),
            types,
            source
        );

        assertThat(pkg.name()).isEqualTo("ComplexPackage");
        assertThat(pkg.types()).hasSize(3);
        assertThat(pkg.types().keySet()).containsExactlyInAnyOrder(
            "ComplexClass", "ComplexEnum", "ComplexPrimitive"
        );
        assertThat(pkg.source().standard()).hasValue("AUTOSAR");
    }

    @Test
    @DisplayName("Should handle attribute with all optional fields")
    void shouldHandleAttributeWithAllOptionalFields() {
        Attribute attr = new Attribute(
            "fullAttribute",
            "CustomType",
            java.util.Optional.of("defaultValue"),
            java.util.Optional.of("0..1")
        );

        assertThat(attr.name()).isEqualTo("fullAttribute");
        assertThat(attr.type()).isEqualTo("CustomType");
        assertThat(attr.defaultValue()).hasValue("defaultValue");
        assertThat(attr.multiplicity()).hasValue("0..1");
    }

    @Test
    @DisplayName("Should handle enumeration literal with all fields")
    void shouldHandleEnumerationLiteralWithAllFields() {
        EnumerationLiteral literal = new EnumerationLiteral(
            "FULL_LITERAL",
            java.util.Optional.of("42"),
            java.util.Optional.of("This is a complete description of the literal")
        );

        assertThat(literal.name()).isEqualTo("FULL_LITERAL");
        assertThat(literal.value()).hasValue("42");
        assertThat(literal.description()).hasValue("This is a complete description of the literal");
    }

    @Test
    @DisplayName("Should handle abstract classes")
    void shouldHandleAbstractClasses() {
        AutosarClass abstractClass = new AutosarClass(
            "AbstractBase", true, "ApplicationType",
            List.of(), List.of(), null, null, List.of()
        );

        AutosarClass concreteClass = new AutosarClass(
            "ConcreteImpl", false, "ApplicationType",
            List.of(), List.of(), null, null, List.of()
        );

        assertThat(abstractClass.isAbstract()).isTrue();
        assertThat(concreteClass.isAbstract()).isFalse();
    }

    @Test
    @DisplayName("Should handle circular dependency detection")
    void shouldHandleCircularDependencyDetection() {
        // Create circular reference: A -> B -> A
        AutosarClass classA = new AutosarClass(
            "ClassA", false, "ApplicationType",
            List.of(), List.of("ClassB"), null, null, List.of()
        );

        AutosarClass classB = new AutosarClass(
            "ClassB", false, "ApplicationType",
            List.of(), List.of("ClassA"), null, null, List.of()
        );

        DocumentSource source = new DocumentSource("circular.pdf", 1, Optional.empty(), Optional.empty());
        AutosarPackage pkg = new AutosarPackage(
            "CircularPackage", List.of("M2"),
            Map.of("ClassA", classA, "ClassB", classB), source
        );

        ParentResolver resolver = new ParentResolver();
        // Should not throw, but handle circular references gracefully
        assertThatCode(() -> resolver.resolveParents(List.of(pkg)))
            .doesNotThrowAnyException();
    }
}
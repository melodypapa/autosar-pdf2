package com.autosar.pdf.unit.parser;

import com.autosar.pdf.parser.ParentResolver;
import com.autosar.pdf.domain.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

class ParentResolverTest {
    @Test
    void shouldResolveParentReferences() {
        DocumentSource source = new DocumentSource("test.pdf", 1, Optional.empty(), Optional.empty());
        AutosarClass parent = new AutosarClass(
            "Parent", false, "ApplicationType",
            List.of(), List.of(), null, null, List.of()
        );
        AutosarClass child = new AutosarClass(
            "Child", false, "ApplicationType",
            List.of(), List.of("Parent"), null, null, List.of()
        );

        AutosarPackage pkg = new AutosarPackage(
            "TestPackage", List.of("M2"),
            Map.of("Parent", parent, "Child", child), source
        );

        ParentResolver resolver = new ParentResolver();
        resolver.resolveParents(List.of(pkg));

        // Use getChildren method since records are immutable
        assertThat(resolver.getChildren("Parent")).hasSize(1);
        assertThat(resolver.getChildren("Parent").get(0)).isEqualTo(child);
    }

    @Test
    void shouldHandleMultipleChildren() {
        DocumentSource source = new DocumentSource("test.pdf", 1, Optional.empty(), Optional.empty());
        AutosarClass parent = new AutosarClass(
            "Parent", false, "ApplicationType",
            List.of(), List.of(), null, null, List.of()
        );
        AutosarClass child1 = new AutosarClass(
            "Child1", false, "ApplicationType",
            List.of(), List.of("Parent"), null, null, List.of()
        );
        AutosarClass child2 = new AutosarClass(
            "Child2", false, "ApplicationType",
            List.of(), List.of("Parent"), null, null, List.of()
        );

        AutosarPackage pkg = new AutosarPackage(
            "TestPackage", List.of("M2"),
            Map.of("Parent", parent, "Child1", child1, "Child2", child2), source
        );

        ParentResolver resolver = new ParentResolver();
        resolver.resolveParents(List.of(pkg));

        assertThat(resolver.getChildren("Parent")).hasSize(2);
        assertThat(resolver.getChildren("Parent")).contains(child1, child2);
    }

    @Test
    void shouldHandleMissingParent() {
        DocumentSource source = new DocumentSource("test.pdf", 1, Optional.empty(), Optional.empty());
        AutosarClass orphan = new AutosarClass(
            "Orphan", false, "ApplicationType",
            List.of(), List.of("NonExistentParent"), Optional.empty(), Optional.empty(), List.of()
        );

        AutosarPackage pkg = new AutosarPackage(
            "TestPackage", List.of("M2"),
            Map.of("Orphan", orphan), source
        );

        ParentResolver resolver = new ParentResolver();
        resolver.resolveParents(List.of(pkg));

        // Should not throw, just handle missing parent gracefully
        // The orphan class should still be tracked in the class map
        assertThat(resolver.hasClass("Orphan")).isTrue();
        // The child is tracked as having NonExistentParent as a base
        assertThat(resolver.getChildren("NonExistentParent")).containsExactly(orphan);
    }
}
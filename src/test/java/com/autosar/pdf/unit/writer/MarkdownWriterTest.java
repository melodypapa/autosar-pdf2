package com.autosar.pdf.unit.writer;

import com.autosar.pdf.writer.MarkdownWriter;
import com.autosar.pdf.writer.OutputWriter;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

class MarkdownWriterTest {
    @Test
    void shouldWriteMapping() {
        MarkdownWriter writer = new MarkdownWriter();
        String mapping = (String) writer.writeMapping(Map.of("Class1", "Package1"));
        assertThat(mapping).contains("Class1");
        assertThat(mapping).contains("Package1");
    }

    @Test
    void shouldWriteClassDetails() {
        MarkdownWriter writer = new MarkdownWriter();
        Object result = writer.writeClassDetails(new com.autosar.pdf.domain.AutosarClass(
            "TestClass", false, "ApplicationType",
            java.util.List.of(), java.util.List.of(), Optional.empty(), Optional.empty(), java.util.List.of()
        ));
        String details = result.toString();
        assertThat(details).contains("# TestClass");
    }

    @Test
    void shouldWriteInheritanceHierarchy() {
        MarkdownWriter writer = new MarkdownWriter();
        String hierarchy = (String) writer.writeInheritanceHierarchy(java.util.List.of());
        assertThat(hierarchy).contains("# Inheritance Hierarchy");
    }
}
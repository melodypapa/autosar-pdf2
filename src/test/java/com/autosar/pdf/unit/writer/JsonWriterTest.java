package com.autosar.pdf.unit.writer;

import com.autosar.pdf.writer.JsonWriter;
import com.autosar.pdf.writer.OutputWriter;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class JsonWriterTest {
    @Test
    void shouldWriteMappingAsJson() {
        JsonWriter writer = new JsonWriter();
        var mapping = (com.fasterxml.jackson.databind.JsonNode) writer.writeMapping(Map.of("Class1", "Package1"));
        assertThat(mapping.get("Class1").asText()).isEqualTo("Package1");
    }

    @Test
    void shouldWriteClassDetailsAsJson() throws Exception {
        JsonWriter writer = new JsonWriter();
        var detailsJson = (String) writer.writeClassDetails(new com.autosar.pdf.domain.AutosarClass(
            "TestClass", false, "ApplicationType",
            java.util.List.of(), java.util.List.of(), null, null, java.util.List.of()
        ));
        // Parse the JSON string to verify structure
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        var details = mapper.readTree(detailsJson);
        assertThat(details.get("name").asText()).isEqualTo("TestClass");
        assertThat(details.get("atpType").asText()).isEqualTo("ApplicationType");
    }

    @Test
    void shouldWriteInheritanceHierarchyAsJson() {
        JsonWriter writer = new JsonWriter();
        var hierarchy = (com.fasterxml.jackson.databind.JsonNode) writer.writeInheritanceHierarchy(java.util.List.of());
        assertThat(hierarchy.isArray()).isTrue();
    }
}
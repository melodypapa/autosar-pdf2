package com.autosar.pdf.writer;

import com.autosar.pdf.domain.AutosarClass;
import com.fasterxml.jackson.databind.JsonNode;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Interface for writing extracted AUTOSAR data to various formats.
 */
public interface OutputWriter {
    /**
     * Writes package hierarchy to a file.
     *
     * @param path Output file path
     */
    void writePackageHierarchy(Path path);

    /**
     * Writes class details.
     *
     * @param cls Class to write
     * @return Formatted class details (String for Markdown, String/JsonNode for JSON)
     */
    Object writeClassDetails(AutosarClass cls);

    /**
     * Writes type-to-package mapping.
     *
     * @param mapping Map of type names to package paths
     * @return Formatted mapping (String for Markdown, JsonNode for JSON)
     */
    Object writeMapping(Map<String, String> mapping);

    /**
     * Writes inheritance hierarchy.
     *
     * @param classes List of classes to include in hierarchy
     * @return Formatted hierarchy (String for Markdown, JsonNode for JSON)
     */
    Object writeInheritanceHierarchy(List<AutosarClass> classes);
}
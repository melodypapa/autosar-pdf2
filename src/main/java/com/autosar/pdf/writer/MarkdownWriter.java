package com.autosar.pdf.writer;

import com.autosar.pdf.domain.AutosarClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Markdown writer for AUTOSAR data.
 */
public class MarkdownWriter implements OutputWriter {

    @Override
    public void writePackageHierarchy(Path path) {
        // TODO: Implement package hierarchy writing
        try {
            String content = "# Package Hierarchy\n\nTODO: Implement";
            Files.writeString(path, content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write package hierarchy: " + path, e);
        }
    }

    @Override
    public Object writeClassDetails(AutosarClass cls) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(cls.name()).append("\n\n");

        sb.append("**Type:** ").append(cls.atpType()).append("\n");
        if (cls.isAbstract()) {
            sb.append("**Abstract:** Yes\n");
        }

        List<String> bases = cls.bases();
        if (!bases.isEmpty()) {
            sb.append("**Base Classes:** ").append(String.join(", ", bases)).append("\n");
        }

        var attributes = cls.attributes();
        if (!attributes.isEmpty()) {
            sb.append("\n## Attributes\n\n");
            sb.append("| Name | Type | Default | Multiplicity |\n");
            sb.append("|------|------|---------|--------------|\n");
            for (var attr : attributes) {
                sb.append("| ")
                  .append(attr.name()).append(" | ")
                  .append(attr.type()).append(" | ")
                  .append(attr.defaultValue().orElse("")).append(" | ")
                  .append(attr.multiplicity().orElse("")).append(" |\n");
            }
        }

        return sb.toString();
    }

    @Override
    public Object writeMapping(Map<String, String> mapping) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Type-to-Package Mapping\n\n");

        // Sort by type name
        mapping.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .toList()
            .forEach(entry -> {
                sb.append("- **").append(entry.getKey()).append("**: ")
                  .append(entry.getValue()).append("\n");
            });

        return sb.toString();
    }

    @Override
    public Object writeInheritanceHierarchy(List<AutosarClass> classes) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Inheritance Hierarchy\n\n");

        // Group by package and sort
        Map<String, List<AutosarClass>> byPackage = classes.stream()
            .collect(Collectors.groupingBy(cls -> cls.name() + " (package unknown)"));

        for (var entry : byPackage.entrySet()) {
            sb.append("## ").append(entry.getKey()).append("\n\n");
            // TODO: Implement tree structure for inheritance
            for (var cls : entry.getValue()) {
                sb.append("- ").append(cls.name());
                if (!cls.bases().isEmpty()) {
                    sb.append(" (extends ").append(String.join(", ", cls.bases())).append(")");
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }
}
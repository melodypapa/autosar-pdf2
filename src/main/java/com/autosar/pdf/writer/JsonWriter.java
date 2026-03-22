package com.autosar.pdf.writer;

import com.autosar.pdf.domain.AutosarClass;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * JSON writer for AUTOSAR data.
 */
public class JsonWriter implements OutputWriter {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void writePackageHierarchy(Path path) {
        // TODO: Implement package hierarchy writing
        try {
            ObjectNode root = MAPPER.createObjectNode();
            root.put("message", "TODO: Implement package hierarchy");
            Files.writeString(path, MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(root));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write package hierarchy: " + path, e);
        }
    }

    @Override
    public Object writeClassDetails(AutosarClass cls) {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("name", cls.name());
        root.put("isAbstract", cls.isAbstract());
        root.put("atpType", cls.atpType());

        if (!cls.bases().isEmpty()) {
            ArrayNode basesArray = root.putArray("bases");
            cls.bases().forEach(basesArray::add);
        }

        if (!cls.attributes().isEmpty()) {
            ArrayNode attrsArray = root.putArray("attributes");
            for (var attr : cls.attributes()) {
                ObjectNode attrNode = attrsArray.addObject();
                attrNode.put("name", attr.name());
                attrNode.put("type", attr.type());
                attr.defaultValue().ifPresent(v -> attrNode.put("defaultValue", v));
                attr.multiplicity().ifPresent(v -> attrNode.put("multiplicity", v));
            }
        }

        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (IOException e) {
            return "{}";
        }
    }

    @Override
    public Object writeMapping(Map<String, String> mapping) {
        ObjectNode root = MAPPER.createObjectNode();
        mapping.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .toList()
            .forEach(entry -> root.put(entry.getKey(), entry.getValue()));
        return root;
    }

    @Override
    public Object writeInheritanceHierarchy(List<AutosarClass> classes) {
        ArrayNode root = MAPPER.createArrayNode();

        for (var cls : classes) {
            ObjectNode classNode = root.addObject();
            classNode.put("name", cls.name());
            classNode.put("atpType", cls.atpType());

            if (!cls.bases().isEmpty()) {
                ArrayNode basesArray = classNode.putArray("bases");
                cls.bases().forEach(basesArray::add);
            }
        }

        return root;
    }
}
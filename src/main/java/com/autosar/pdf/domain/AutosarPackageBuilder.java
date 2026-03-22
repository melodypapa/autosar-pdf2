package com.autosar.pdf.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder for constructing AutosarPackage instances with mutable state during parsing.
 * Allows incremental addition of types and setting of source information.
 */
public class AutosarPackageBuilder {
    private final String name;
    private final List<String> path;
    private final Map<String, AutosarType> types = new HashMap<>();
    private DocumentSource source;

    public AutosarPackageBuilder(String name, List<String> path) {
        this.name = name;
        this.path = new ArrayList<>(path);
    }

    public void addType(AutosarType type) {
        types.put(type.name(), type);
    }

    public void setSource(DocumentSource source) {
        this.source = source;
    }

    public AutosarPackage build() {
        return new AutosarPackage(name, path, types, source);
    }
}
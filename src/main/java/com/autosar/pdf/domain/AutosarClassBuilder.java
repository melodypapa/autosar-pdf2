package com.autosar.pdf.domain;

import java.util.*;
import java.util.Optional;

/**
 * Builder for constructing AutosarClass instances with mutable state during parsing.
 * Used by the parsing layer to incrementally build class definitions across multiple pages.
 */
public class AutosarClassBuilder {
    private String name;
    private boolean isAbstract = false;
    private String atpType;
    private final List<Attribute> attributes = new ArrayList<>();
    private final List<String> bases = new ArrayList<>();
    private AutosarClass parent;
    private String aggregatedBy;
    private final List<String> subclasses = new ArrayList<>();

    public AutosarClassBuilder(String name, String atpType) {
        this.name = name;
        this.atpType = atpType;
    }

    public AutosarClassBuilder isAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
        return this;
    }

    public AutosarClassBuilder addAttribute(Attribute attribute) {
        this.attributes.add(attribute);
        return this;
    }

    public AutosarClassBuilder addBase(String base) {
        this.bases.add(base);
        return this;
    }

    public AutosarClassBuilder parent(AutosarClass parent) {
        this.parent = parent;
        return this;
    }

    public AutosarClassBuilder aggregatedBy(String aggregatedBy) {
        this.aggregatedBy = aggregatedBy;
        return this;
    }

    public AutosarClassBuilder addSubclass(String subclass) {
        this.subclasses.add(subclass);
        return this;
    }

    public AutosarClass build() {
        return new AutosarClass(
            name,
            isAbstract,
            atpType,
            List.copyOf(attributes),
            List.copyOf(bases),
            Optional.ofNullable(parent),
            Optional.ofNullable(aggregatedBy),
            List.copyOf(subclasses)
        );
    }
}
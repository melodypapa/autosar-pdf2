package com.autosar.pdf.domain;

import java.util.List;
import java.util.Optional;

public record AutosarClass(
    String name,
    boolean isAbstract,
    String atpType,
    List<Attribute> attributes,
    List<String> bases,
    Optional<AutosarClass> parent,
    Optional<String> aggregatedBy,
    List<String> subclasses
) implements AutosarType {
}
package com.autosar.pdf.domain;

import java.util.List;

public record AutosarPrimitive(
    String name,
    List<Attribute> attributes
) implements AutosarType {
}
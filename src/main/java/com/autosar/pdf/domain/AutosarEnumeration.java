package com.autosar.pdf.domain;

import java.util.List;

public record AutosarEnumeration(
    String name,
    List<EnumerationLiteral> literals
) implements AutosarType {}
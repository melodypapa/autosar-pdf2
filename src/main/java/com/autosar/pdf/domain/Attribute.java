package com.autosar.pdf.domain;

import java.util.Optional;

public record Attribute(
    String name,
    String type,
    Optional<String> defaultValue,
    Optional<String> multiplicity
) {}
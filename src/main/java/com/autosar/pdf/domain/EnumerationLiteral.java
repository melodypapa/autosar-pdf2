package com.autosar.pdf.domain;

import java.util.Optional;

public record EnumerationLiteral(
    String name,
    Optional<String> value,
    Optional<String> description
) {
}
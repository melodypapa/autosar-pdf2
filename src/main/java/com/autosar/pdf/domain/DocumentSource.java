package com.autosar.pdf.domain;

import java.util.Optional;

public record DocumentSource(
    String filename,
    int page,
    Optional<String> standard,
    Optional<String> release
) {}
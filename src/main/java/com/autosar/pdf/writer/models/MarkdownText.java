package com.autosar.pdf.writer.models;

import java.util.Optional;

/**
 * Represents text extracted from a PDF for Markdown conversion.
 */
public record MarkdownText(
    String content,
    int pageNumber,
    Optional<String> position,
    TextType type
) {
}
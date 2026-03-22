package com.autosar.pdf.writer.models;

import java.util.List;
import java.util.Optional;

/**
 * Represents a table extracted from a PDF for Markdown conversion.
 */
public record MarkdownTable(
    List<String> headers,
    List<List<String>> rows,
    int pageNumber,
    Optional<List<Alignment>> cellAlignment
) {
}
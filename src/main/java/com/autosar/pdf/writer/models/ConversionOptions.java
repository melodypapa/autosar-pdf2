package com.autosar.pdf.writer.models;

/**
 * Conversion options for PDF to Markdown conversion.
 */
public record ConversionOptions(
    boolean preserveTitles,
    boolean tableOnly,
    boolean insertPageBreaks,
    boolean verbose
) {
}
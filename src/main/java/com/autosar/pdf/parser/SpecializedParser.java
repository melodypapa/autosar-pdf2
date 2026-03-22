package com.autosar.pdf.parser;

/**
 * Interface for specialized parsers that handle specific AUTOSAR element patterns.
 *
 * Each parser is responsible for recognizing and parsing a specific type of
 * AUTOSAR element from extracted PDF text.
 */
public interface SpecializedParser {
    /**
     * Checks if this parser can handle the given line of text.
     *
     * @param line A line of text from the extracted PDF
     * @return true if this parser can parse this line
     */
    boolean canParse(String line);

    /**
     * Parses the line and updates the parse context accordingly.
     *
     * @param line A line of text from the extracted PDF
     * @param context Current parsing context (mutable, will be updated)
     */
    void parse(String line, ParseContext context);
}
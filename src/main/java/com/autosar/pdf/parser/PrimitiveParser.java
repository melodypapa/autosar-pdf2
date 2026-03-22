package com.autosar.pdf.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for AUTOSAR primitive type definitions.
 *
 * Pattern: [Pp]rimitive <Name>
 * Examples:
 * - "Primitive String"
 * - "Primitive Boolean"
 *
 * Primitives are basic types that may have attributes but no inheritance hierarchy.
 */
public class PrimitiveParser implements SpecializedParser {
    private static final Pattern PRIMITIVE_PATTERN = Pattern.compile(
        "\\b[Pp]rimitive\\s+(\\w+)"
    );

    @Override
    public boolean canParse(String line) {
        return PRIMITIVE_PATTERN.matcher(line).find();
    }

    @Override
    public void parse(String line, ParseContext context) {
        Matcher matcher = PRIMITIVE_PATTERN.matcher(line);
        if (matcher.find()) {
            String primitiveName = matcher.group(1);

            // TODO: Create new primitive and update context
            // This requires:
            // - Creating AutosarPrimitive with empty attributes list
            // - Adding to current package builder
            // - Subsequent table rows will add attributes
        }
    }
}
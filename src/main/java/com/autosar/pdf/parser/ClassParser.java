package com.autosar.pdf.parser;

import com.autosar.pdf.domain.AutosarClassBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for AUTOSAR class definitions (ApplicationType and ApplicationDataType).
 *
 * Pattern: [Aa]pplication(T|D)Type <Name>
 * Examples:
 * - "ApplicationType SwComponentType"
 * - "ApplicationDataType SwRecord"
 *
 * Class definitions are recognized by section header pattern (preceded by blank line + newline)
 * and appear at section start, not mid-paragraph.
 */
public class ClassParser implements SpecializedParser {
    private static final Pattern CLASS_PATTERN = Pattern.compile(
        "\\b([Aa]pplication(?:Type|DataType))\\s+(\\w+)"
    );

    private static final String ABSTRACT_MARKER = "(abstract)";

    @Override
    public boolean canParse(String line) {
        return CLASS_PATTERN.matcher(line).find();
    }

    @Override
    public void parse(String line, ParseContext context) {
        Matcher matcher = CLASS_PATTERN.matcher(line);

        if (matcher.find()) {
            String atpType = matcher.group(1);
            String className = matcher.group(2);

            // Determine if abstract
            boolean isAbstract = line.toLowerCase().contains(ABSTRACT_MARKER);

            // Create new class builder and update context
            AutosarClassBuilder classBuilder = new AutosarClassBuilder(className, atpType)
                .isAbstract(isAbstract);

            // TODO: Update context with new class builder
            // This requires modifying ParseContext to use a mutable context
            // For now, the skeleton is in place for when context handling is implemented
        }
    }
}
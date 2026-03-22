package com.autosar.pdf.parser;

/**
 * Handler for merging multi-line attribute names in PDF tables.
 *
 * Attribute names can be split across PDF lines in various ways:
 * - Hyphenated word breaks: "re-" + "quest2Support" → "request2Support"
 * - CamelCase fragments: "bswModule" + "Documentation" → "bswModuleDocumentation"
 * - Simple concatenation for unambiguous cases
 */
public class AttributeMultiLineHandler {

    /**
     * Merges multiple attribute name fragments into a single name.
     *
     * @param fragments Two or more attribute name fragments
     * @return Merged attribute name
     */
    public String mergeAttributeNames(String... fragments) {
        if (fragments == null || fragments.length == 0) {
            return "";
        }

        if (fragments.length == 1) {
            return fragments[0];
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < fragments.length; i++) {
            String fragment = fragments[i];

            // Remove hyphens and append the fragment
            if (fragment.endsWith("-")) {
                result.append(fragment.substring(0, fragment.length() - 1));
            } else {
                result.append(fragment);
            }
        }

        return result.toString();
    }

    /**
     * Merges two attribute name fragments.
     *
     * @param line1 First fragment
     * @param line2 Second fragment
     * @return Merged attribute name
     */
    public String mergeAttributeNames(String line1, String line2) {
        return mergeAttributeNames(new String[]{line1, line2});
    }
}
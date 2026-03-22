package com.autosar.pdf.unit.parser;

import com.autosar.pdf.parser.AttributeMultiLineHandler;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AttributeMultiLineHandlerTest {
    @Test
    void shouldMergeHyphenatedAttributeNames() {
        AttributeMultiLineHandler handler = new AttributeMultiLineHandler();
        String result = handler.mergeAttributeNames("re-", "quest2Support");
        assertThat(result).isEqualTo("request2Support");
    }

    @Test
    void shouldMergeCamelCaseFragments() {
        AttributeMultiLineHandler handler = new AttributeMultiLineHandler();
        String result = handler.mergeAttributeNames("bswModule", "Documentation");
        assertThat(result).isEqualTo("bswModuleDocumentation");
    }

    @Test
    void shouldMergeHyphenatedWordBreaks() {
        AttributeMultiLineHandler handler = new AttributeMultiLineHandler();
        String result = handler.mergeAttributeNames("Sw", "Component");
        assertThat(result).isEqualTo("SwComponent");
    }

    @Test
    void shouldHandleMultipleHyphens() {
        AttributeMultiLineHandler handler = new AttributeMultiLineHandler();
        String result = handler.mergeAttributeNames("pre-", "fix-", "ed");
        assertThat(result).isEqualTo("prefixed");
    }
}
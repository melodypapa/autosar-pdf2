package com.autosar.pdf.unit.parser;

import com.autosar.pdf.parser.ParseContext;
import com.autosar.pdf.domain.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ParseContextTest {
    @Test
    void shouldCreateParseContext() {
        ParseContext context = new ParseContext(
            null, null, 1, null, java.util.List.of()
        );
        assertThat(context.currentPage()).isEqualTo(1);
        assertThat(context.packages()).isEmpty();
    }
}
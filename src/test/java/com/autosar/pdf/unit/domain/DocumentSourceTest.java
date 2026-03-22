package com.autosar.pdf.unit.domain;

import com.autosar.pdf.domain.DocumentSource;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

class DocumentSourceTest {
    @Test
    void shouldCreateDocumentSourceWithAllFields() {
        DocumentSource source = new DocumentSource(
            "test.pdf", 42, Optional.of("AUTOSAR"), Optional.of("R23-11")
        );
        assertThat(source.filename()).isEqualTo("test.pdf");
        assertThat(source.page()).isEqualTo(42);
        assertThat(source.standard()).hasValue("AUTOSAR");
        assertThat(source.release()).hasValue("R23-11");
    }

    @Test
    void shouldCreateDocumentSourceWithOptionalFields() {
        DocumentSource source = new DocumentSource(
            "test.pdf", 42, Optional.empty(), Optional.empty()
        );
        assertThat(source.standard()).isEmpty();
        assertThat(source.release()).isEmpty();
    }
}
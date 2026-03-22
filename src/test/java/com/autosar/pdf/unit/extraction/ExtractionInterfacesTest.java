package com.autosar.pdf.unit.extraction;

import com.autosar.pdf.domain.AutosarDoc;
import com.autosar.pdf.extraction.PdfExtractor;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class ExtractionInterfacesTest {
    @Test
    void shouldHavePdfExtractorInterface() {
        PdfExtractor extractor = createMockExtractor();
        AutosarDoc doc = extractor.extract("test.pdf");
        assertThat(doc).isNotNull();
    }

    private PdfExtractor createMockExtractor() {
        return new PdfExtractor() {
            @Override
            public AutosarDoc extract(String pdfPath) {
                return new AutosarDoc(List.of());
            }
        };
    }
}
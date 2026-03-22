package com.autosar.pdf.unit.extraction;

import com.autosar.pdf.domain.AutosarDoc;
import com.autosar.pdf.extraction.TwoPhaseExtractor;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class TwoPhaseExtractorTest {
    @Test
    void shouldExtractPdf() {
        TwoPhaseExtractor extractor = new TwoPhaseExtractor();
        AutosarDoc doc = extractor.extract("examples/pdf/AUTOSAR_FO_TPS_GenericStructureTemplate.pdf");
        assertThat(doc).isNotNull();
    }
}
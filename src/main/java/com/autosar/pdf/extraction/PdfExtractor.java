package com.autosar.pdf.extraction;

import com.autosar.pdf.domain.AutosarDoc;

public interface PdfExtractor {
    AutosarDoc extract(String pdfPath);
}
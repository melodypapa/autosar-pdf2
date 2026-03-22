package com.autosar.pdf.extraction;

public interface TextExtractor {
    String extract(String pdfPath, int pageNumber);
}
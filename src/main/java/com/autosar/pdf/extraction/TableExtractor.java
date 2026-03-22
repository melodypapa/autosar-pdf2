package com.autosar.pdf.extraction;

import java.util.List;

public interface TableExtractor {
    List<List<String>> extract(String pdfPath, int pageNumber);
}
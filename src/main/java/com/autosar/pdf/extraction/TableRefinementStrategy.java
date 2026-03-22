package com.autosar.pdf.extraction;

import java.util.List;

/**
 * Strategy interface for refining extracted table cells based on positional information.
 * Uses text position data to correct cell content assignments when table borders are unclear.
 */
public interface TableRefinementStrategy {
    /**
     * Refines cell content using positional text information.
     *
     * @param cells Raw extracted cells from Tabula
     * @param textPositions Text position data from PDFBox (can be null if unavailable)
     * @return Refined cells with corrected content assignments
     */
    List<List<String>> refineCells(List<List<String>> cells, List<?> textPositions);
}
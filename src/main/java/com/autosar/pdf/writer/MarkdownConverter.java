package com.autosar.pdf.writer;

import com.autosar.pdf.writer.models.ConversionOptions;
import com.autosar.pdf.writer.models.MarkdownTable;
import com.autosar.pdf.writer.models.MarkdownText;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.Loader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Converter for transforming PDF documents to Markdown format.
 *
 * This converter uses Tabula-java for table extraction (LATTICE mode) and
 * PDFBox for text extraction. Tables are converted to Markdown tables,
 * and text is preserved as paragraphs and headings.
 */
public class MarkdownConverter {

    /**
     * Converts a PDF file to Markdown format.
     *
     * @param pdfPath Path to the input PDF file
     * @param options Conversion options
     * @return Markdown string representation of the PDF
     */
    public String convertPdfToMarkdown(Path pdfPath, ConversionOptions options) {
        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            List<MarkdownText> text;
            if (!options.tableOnly()) {
                text = extractText(pdfPath, options);
            } else {
                text = List.of();
            }

            List<MarkdownTable> tables = extractTables(pdfPath, options);

            return combineContent(tables, text, options);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert PDF: " + pdfPath, e);
        }
    }

    /**
     * Extracts tables from a PDF using Tabula in LATTICE mode.
     *
     * @param pdfPath Path to the PDF file
     * @param options Conversion options
     * @return List of extracted tables
     */
    private List<MarkdownTable> extractTables(Path pdfPath, ConversionOptions options) {
        // TODO: Implement Tabula LATTICE extraction
        // - Use ObjectExtractor.create(pdfDocument)
        // - Extract tables using ExtractionMethod.LATTICE
        // - Fallback to PDFBox if no tables detected
        // - Merge multi-page tables
        // - Apply positional refinement

        return new ArrayList<>();
    }

    /**
     * Extracts text from a PDF using PDFBox.
     *
     * @param pdfPath Path to the PDF file
     * @param options Conversion options
     * @return List of extracted text elements
     */
    private List<MarkdownText> extractText(Path pdfPath, ConversionOptions options) {
        // TODO: Implement PDFBox text extraction
        // - Extract text with position information
        // - Detect headings based on font size/position
        // - Detect list items
        // - Convert to MarkdownText objects

        return new ArrayList<>();
    }

    /**
     * Combines tables and text into a single Markdown document.
     *
     * @param tables Extracted tables
     * @param text Extracted text elements
     * @param options Conversion options
     * @return Combined Markdown content
     */
    private String combineContent(List<MarkdownTable> tables, List<MarkdownText> text, ConversionOptions options) {
        StringBuilder sb = new StringBuilder();

        // Sort content by page number
        List<MarkdownTable> sortedTables = tables.stream()
            .sorted(Comparator.comparingInt(MarkdownTable::pageNumber))
            .toList();

        List<MarkdownText> sortedText = text.stream()
            .sorted(Comparator.comparingInt(MarkdownText::pageNumber))
            .toList();

        // Combine content in page order
        // TODO: Implement proper content ordering and merging
        // - Merge tables and text by page number
        // - Insert page breaks if option is enabled
        // - Convert tables to Markdown format
        // - Convert text to appropriate Markdown elements

        sb.append("# Converted PDF\n\n");
        sb.append("<!-- TODO: Implement full conversion logic -->\n\n");

        return sb.toString();
    }

    /**
     * Converts a MarkdownTable to Markdown string format.
     *
     * @param table Table to convert
     * @return Markdown string
     */
    private String tableToMarkdown(MarkdownTable table) {
        StringBuilder sb = new StringBuilder();

        // Headers
        sb.append("| ");
        sb.append(String.join(" | ", table.headers()));
        sb.append(" |\n");

        // Separator row
        sb.append("| ");
        for (int i = 0; i < table.headers().size(); i++) {
            sb.append("---");
            if (i < table.headers().size() - 1) {
                sb.append(" | ");
            }
        }
        sb.append(" |\n");

        // Rows
        for (List<String> row : table.rows()) {
            sb.append("| ");
            sb.append(String.join(" | ", row));
            sb.append(" |\n");
        }

        return sb.toString();
    }

    /**
     * Converts a MarkdownText element to Markdown string format.
     *
     * @param text Text element to convert
     * @return Markdown string
     */
    private String textToMarkdown(MarkdownText text) {
        return switch (text.type()) {
            case HEADING -> "## " + text.content() + "\n\n";
            case PARAGRAPH -> text.content() + "\n\n";
            case LIST_ITEM -> "- " + text.content() + "\n";
        };
    }
}
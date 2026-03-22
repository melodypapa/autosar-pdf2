package com.autosar.pdf.unit.writer;

import com.autosar.pdf.writer.MarkdownConverter;
import com.autosar.pdf.writer.models.ConversionOptions;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class MarkdownConverterTest {
    @Test
    void shouldConvertPdfToMarkdown() {
        MarkdownConverter converter = new MarkdownConverter();
        ConversionOptions options = new ConversionOptions(true, false, true, false);
        String markdown = converter.convertPdfToMarkdown(
            Path.of("examples/pdf/AUTOSAR_FO_TPS_GenericStructureTemplate.pdf"),
            options
        );
        assertThat(markdown).isNotEmpty();
        // Returns skeleton content for now
    }

    @Test
    void shouldHandleTableOnlyOption() {
        MarkdownConverter converter = new MarkdownConverter();
        ConversionOptions options = new ConversionOptions(false, true, false, false);
        String markdown = converter.convertPdfToMarkdown(
            Path.of("examples/pdf/AUTOSAR_FO_TPS_GenericStructureTemplate.pdf"),
            options
        );
        assertThat(markdown).isNotEmpty();
    }

    @Test
    void shouldHandleMissingFile() {
        MarkdownConverter converter = new MarkdownConverter();
        ConversionOptions options = new ConversionOptions(true, false, true, false);
        assertThatExceptionOfType(RuntimeException.class)
            .isThrownBy(() ->
                converter.convertPdfToMarkdown(Path.of("nonexistent.pdf"), options)
            );
    }
}
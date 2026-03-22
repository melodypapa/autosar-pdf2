package com.autosar.pdf.unit.writer;

import com.autosar.pdf.writer.models.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

class MarkdownConverterModelsTest {
    @Test
    void shouldCreateMarkdownTable() {
        MarkdownTable table = new MarkdownTable(
            List.of("Name", "Type"),
            List.of(List.of("Test", "String")),
            1,
            Optional.of(List.of(Alignment.LEFT, Alignment.LEFT))
        );
        assertThat(table.headers()).hasSize(2);
        assertThat(table.pageNumber()).isEqualTo(1);
    }

    @Test
    void shouldCreateMarkdownText() {
        MarkdownText text = new MarkdownText(
            "Sample text",
            1,
            Optional.of("top"),
            TextType.PARAGRAPH
        );
        assertThat(text.content()).isEqualTo("Sample text");
        assertThat(text.type()).isEqualTo(TextType.PARAGRAPH);
    }

    @Test
    void shouldCreateConversionOptions() {
        ConversionOptions options = new ConversionOptions(
            true, false, true, false
        );
        assertThat(options.preserveTitles()).isTrue();
        assertThat(options.tableOnly()).isFalse();
    }

    @Test
    void shouldHaveAllAlignmentTypes() {
        Alignment[] alignments = Alignment.values();
        assertThat(alignments).containsExactly(Alignment.LEFT, Alignment.CENTER, Alignment.RIGHT);
    }

    @Test
    void shouldHaveAllTextTypes() {
        TextType[] types = TextType.values();
        assertThat(types).containsExactly(TextType.HEADING, TextType.PARAGRAPH, TextType.LIST_ITEM);
    }
}
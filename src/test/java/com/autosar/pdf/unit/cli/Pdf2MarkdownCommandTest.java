package com.autosar.pdf.unit.cli;

import com.autosar.pdf.cli.Pdf2MarkdownCommand;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class Pdf2MarkdownCommandTest {
    @Test
    void shouldHaveRequiredParameters() {
        Pdf2MarkdownCommand cmd = new Pdf2MarkdownCommand();
        assertThat(cmd.inputPdf).isNull();
        assertThat(cmd.outputMd).isNull();
    }

    @Test
    void shouldInitializeWithDefaultOptions() {
        Pdf2MarkdownCommand cmd = new Pdf2MarkdownCommand();
        assertThat(cmd.preserveTitles).isFalse();
        assertThat(cmd.tableOnly).isFalse();
        assertThat(cmd.verbose).isFalse();
    }
}
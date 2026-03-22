package com.autosar.pdf.unit.cli;

import com.autosar.pdf.Main;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MainTest {
    @Test
    void shouldHandleAutosarExtractCommand() {
        int exitCode = Main.execute(new String[]{"--help"});
        assertThat(exitCode).isZero();
    }

    @Test
    void shouldHandlePdf2MarkdownCommand() {
        int exitCode = Main.execute(new String[]{"pdf2md", "--help"});
        assertThat(exitCode).isZero();
    }

    @Test
    void shouldHandleDefaultCommand() {
        // Default to autosar-extract when no subcommand specified
        int exitCode = Main.execute(new String[]{"--help"});
        assertThat(exitCode).isZero();
    }
}
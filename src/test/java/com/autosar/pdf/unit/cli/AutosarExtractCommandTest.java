package com.autosar.pdf.unit.cli;

import com.autosar.pdf.cli.AutosarExtractCommand;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AutosarExtractCommandTest {
    @Test
    void shouldHaveRequiredOptions() {
        AutosarExtractCommand cmd = new AutosarExtractCommand();
        assertThat(cmd.mappingFile).isNull();
        assertThat(cmd.hierarchyFile).isNull();
        assertThat(cmd.classDetailsDir).isNull();
    }
}
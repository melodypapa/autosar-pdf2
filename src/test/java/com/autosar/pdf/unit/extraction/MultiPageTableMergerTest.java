package com.autosar.pdf.unit.extraction;

import com.autosar.pdf.extraction.MultiPageTableMerger;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class MultiPageTableMergerTest {
    @Test
    void shouldMergeTablesAcrossPages() {
        MultiPageTableMerger merger = new MultiPageTableMerger();
        List<List<String>> page1 = List.of(List.of("A", "B"), List.of("1", "2"));
        List<List<String>> page2 = List.of(List.of("3", "4"));

        List<List<String>> merged = merger.mergeAdjacentTables(page1, page2);
        assertThat(merged).hasSize(3);
    }
}
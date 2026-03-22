package com.autosar.pdf.unit.extraction;

import com.autosar.pdf.extraction.PositionalRefinement;
import com.autosar.pdf.extraction.TableRefinementStrategy;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class PositionalRefinementTest {
    @Test
    void shouldRefineCellsBasedOnPosition() {
        TableRefinementStrategy strategy = new PositionalRefinement();
        List<List<String>> cells = List.of(
            List.of("cell1"),
            List.of("cell2")
        );
        List<List<String>> refined = strategy.refineCells(cells, null);
        assertThat(refined).isNotNull();
    }
}
package com.autosar.pdf.parser;

import com.autosar.pdf.domain.*;

import java.util.List;
import java.util.Optional;

/**
 * Parse context for stateful parsing across page boundaries.
 *
 * This record maintains parsing state while processing PDF content
 * that may span multiple pages, such as class definitions and attribute tables.
 */
public record ParseContext(
    Optional<AutosarPackageBuilder> currentPackage,
    Optional<AutosarClassBuilder> currentClass,
    int currentPage,
    DocumentSource source,
    List<AutosarPackage> packages
) {}
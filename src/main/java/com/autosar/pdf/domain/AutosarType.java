package com.autosar.pdf.domain;

/**
 * Sealed interface for all AUTOSAR type definitions.
 * Only the permitted classes are allowed to implement this interface.
 */
public sealed interface AutosarType permits AutosarClass, AutosarEnumeration, AutosarPrimitive {
    String name();
}
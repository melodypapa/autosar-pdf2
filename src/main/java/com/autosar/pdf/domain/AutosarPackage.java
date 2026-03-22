package com.autosar.pdf.domain;

import java.util.List;
import java.util.Map;

public record AutosarPackage(
    String name,
    List<String> path,
    Map<String, AutosarType> types,
    DocumentSource source
) {}
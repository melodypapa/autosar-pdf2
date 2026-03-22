# AUTOSAR PDF to Markdown/JSON

Java implementation for extracting AUTOSAR models from PDF specification documents. This tool converts AUTOSAR specification PDFs into structured data (Markdown or JSON format) with support for table extraction, multi-page handling, and two CLI interfaces.

## Features

- **Structured Data Extraction**: Extract AUTOSAR classes, enumerations, and primitives from specification PDFs
- **Two CLI Tools**:
  - `autosar-extract`: Extract structured AUTOSAR data for code generation and documentation
  - `pdf2md`: Direct PDF to Markdown conversion with preserved tables
- **Table Extraction**: Uses Tabula-java with LATTICE mode for accurate table detection
- **Multi-Page Handling**: Merges tables spanning multiple pages
- **Multi-Line Cell Handling**: Handles hyphenated word breaks and camelCase fragments
- **Output Formats**: Supports both Markdown and JSON output

## Requirements

- Java 17 or higher
- Maven 3.6 or higher

## Installation

```bash
# Clone the repository
git clone <repository-url>
cd autosar-pdf2

# Install dependencies
mvn clean install
```

## Usage

### autosar-extract - Structured Data Extraction

Extract AUTOSAR classes, enumerations, and primitives with full type information.

```bash
# Generate type-to-package mapping
mvn exec:java -Dexec.mainClass="com.autosar.pdf.Main" \
  -Dexec.args="autosar-extract examples/pdf/ --mapping mapping.md"

# Generate class inheritance hierarchy
mvn exec:java -Dexec.mainClass="com.autosar.pdf.Main" \
  -Dexec.args="autosar-extract examples/pdf/ --hierarchy hierarchy.md"

# Generate individual class details
mvn exec:java -Dexec.mainClass="com.autosar.pdf.Main" \
  -Dexec.args="autosar-extract examples/pdf/ --class-details output/classes/"

# Output in JSON format
mvn exec:java -Dexec.mainClass="com.autosar.pdf.Main" \
  -Dexec.args="autosar-extract examples/pdf/ --mapping mapping.json --json"

# Verbose mode with logging
mvn exec:java -Dexec.mainClass="com.autosar.pdf.Main" \
  -Dexec.args="autosar-extract examples/pdf/ --mapping mapping.md -v --log-file extraction.log"
```

### pdf2md - Direct PDF to Markdown Conversion

Convert entire PDF content to a single Markdown file with tables preserved.

```bash
# Simple conversion
mvn exec:java -Dexec.mainClass="com.autosar.pdf.Main" \
  -Dexec.args="pdf2md examples/pdf/AUTOSAR_FO_TPS_GenericStructureTemplate.pdf output.md"

# With section headers preserved
mvn exec:java -Dexec.mainClass="com.autosar.pdf.Main" \
  -Dexec.args="pdf2md input.pdf output.md --preserve-titles"

# Extract only tables
mvn exec:java -Dexec.mainClass="com.autosar.pdf.Main" \
  -Dexec.args="pdf2md input.pdf tables.md --table-only"

# Verbose mode
mvn exec:java -Dexec.mainClass="com.autosar.pdf.Main" \
  -Dexec.args="pdf2md input.pdf output.md -v --log-file conversion.log"
```

## Building

```bash
# Compile
mvn compile

# Run all tests
mvn test

# Build JAR with dependencies
mvn package

# Run specific test
mvn test -Dtest=DocumentSourceTest

# Run tests with coverage
mvn clean test jacoco:report
```

## Architecture

### Three-Layer Design

```
PDF Files
    ↓
Extraction Layer (PDFBox + Tabula)
    ↓
Domain Layer (Immutable Records)
    ↓
Output Layer (Markdown/JSON)
```

### Domain Models

- **AutosarPackage**: Container for AUTOSAR types with package path metadata
- **AutosarType**: Sealed interface for all AUTOSAR type definitions
  - **AutosarClass**: ApplicationType/ApplicationDataType with attributes and inheritance
  - **AutosarEnumeration**: Enumeration with literal values
  - **AutosarPrimitive**: Primitive types with attributes
- **Attribute**: Class/primitive attributes with optional defaults and multiplicities
- **DocumentSource**: Source tracking (filename, page, standard, release)

### Extraction Strategy

1. **Phase 1**: Extract text and tables with page boundary markers
2. **Phase 2**: Parse with stateful context for multi-page definitions
3. **Positional Refinement**: Correct cell assignments using text position data
4. **Multi-Page Merging**: Combine tables spanning multiple pages

### Output Formats

#### Markdown Output

```markdown
# SwComponentType

**Type:** ApplicationType
**Base Classes:** ARElement, Identifiable

## Attributes

| Name | Type | Default | Multiplicity |
|------|------|---------|--------------|
| shortName | Identifier |  | 1 |
| swComponentTypeCategory | SwComponentTypeCategoryEnum |  | 1 |
```

#### JSON Output

```json
{
  "name": "SwComponentType",
  "isAbstract": false,
  "atpType": "ApplicationType",
  "bases": ["ARElement", "Identifiable"],
  "attributes": [
    {
      "name": "shortName",
      "type": "Identifier",
      "defaultValue": null,
      "multiplicity": "1"
    }
  ]
}
```

## Development

### Running Tests

```bash
# Run all unit tests
mvn test

# Run specific test class
mvn test -Dtest=AutosarPackageTest

# Run tests with verbose output
mvn test -X

# Generate test report
mvn surefire-report:report
```

### Adding New Tests

Tests are organized by layer:

```
src/test/java/com/autosar/pdf/
├── unit/
│   ├── domain/          # Domain model tests
│   ├── extraction/      # Extraction layer tests
│   ├── parser/          # Parser tests
│   ├── writer/          # Writer tests
│   └── cli/             # CLI command tests
└── integration/         # End-to-end integration tests
```

Example test:

```java
package com.autosar.pdf.unit.domain;

import com.autosar.pdf.domain.DocumentSource;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class DocumentSourceTest {
    @Test
    void shouldCreateDocumentSourceWithAllFields() {
        DocumentSource source = new DocumentSource(
            "test.pdf", 42, "AUTOSAR", "R23-11"
        );
        assertThat(source.filename()).isEqualTo("test.pdf");
        assertThat(source.page()).isEqualTo(42);
        assertThat(source.standard()).hasValue("AUTOSAR");
    }
}
```

### Code Structure

```
src/main/java/com/autosar/pdf/
├── domain/          # Core domain models (records, sealed interfaces)
├── extraction/      # PDF extraction layer (PDFBox + Tabula)
├── parser/          # Parsing logic with specialized parsers
├── writer/          # Output generation (Markdown/JSON)
├── writer/models/   # Models for MarkdownConverter
└── cli/             # Command-line interface
```

## Dependencies

- **PDFBox 3.0.2**: PDF text extraction and position data
- **Tabula-java 2.0.2**: Table extraction with LATTICE mode
- **Jackson 2.16.0**: JSON serialization/deserialization
- **Picocli 4.7.5**: CLI argument parsing
- **JUnit 5.10.0**: Testing framework
- **AssertJ 3.25.1**: Fluent assertion library

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the Apache License 2.0.

## Acknowledgments

- Based on AUTOSAR specification document analysis
- Uses Tabula-java for table extraction
- Designed following Domain-First principles
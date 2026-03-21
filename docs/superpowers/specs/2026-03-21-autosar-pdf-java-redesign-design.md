# AUTOSAR PDF to Markdown/JSON - Java Implementation Design

**Date:** 2026-03-21
**Status:** Draft
**Approach:** Domain-First Redesign

## Overview

Redesign of AUTOSAR PDF extraction tool using Java, featuring improved table extraction with Tabula-java (LATTICE mode) and enhanced multi-line cell handling. This is a domain-first redesign based on AUTOSAR specification requirements rather than a direct port of the Python implementation.

## Architecture

### High-Level Data Flow

```
AUTOSAR PDFs → PDF Extraction Layer → Domain Model → Output Layer → Markdown/JSON
                        ↓                        ↓
                    Tabula (tables)         Java Records/Classes
```

### Layer Separation

1. **Extraction Layer** - PDFBox + Tabula for raw text and table extraction from PDFs
2. **Domain Layer** - Rich domain models representing AUTOSAR concepts (classes, enums, packages, attributes)
3. **Output Layer** - Converters to Markdown and JSON formats

### Key Design Decisions

- **Java 17+ records** for immutable domain models
- **Functional-style parsing** where possible
- **Builder pattern** for complex object construction
- **Strategy pattern** for different parser types (class, enum, primitive)
- **Visitor pattern** for output generation

## Domain Model

### Core Domain Entities

```
AutosarPackage (record)
├── name: String
├── path: List<String>  // e.g., ["M2", "AUTOSAR", "CommonStructure"]
├── types: Map<String, AutosarType>
└── source: DocumentSource

AutosarType (interface)
├── AutosarClass
│   ├── name: String
│   ├── isAbstract: boolean
│   ├── atpType: String  // ApplicationType or ApplicationDataType
│   ├── attributes: List<Attribute>
│   ├── bases: List<String>  // parent class names
│   ├── parent: Optional<AutosarClass>
│   ├── aggregatedBy: Optional<String>
│   └── subclasses: List<String>
├── AutosarEnumeration
│   ├── name: String
│   └── literals: List<EnumerationLiteral>
└── AutosarPrimitive
    ├── name: String
    └── attributes: List<Attribute>

Attribute (record)
├── name: String
├── type: String
├── defaultValue: Optional<String>
└── multiplicity: Optional<String>

EnumerationLiteral (record)
├── name: String
├── value: Optional<String>
└── description: Optional<String>

DocumentSource (record)
├── filename: String
├── page: int
├── standard: Optional<String>
└── release: Optional<String>
```

### Design Principles

- Immutable records for thread safety and clarity
- Optional types for nullable fields
- Type-safe with sealed interfaces where appropriate

## Extraction Layer

### Components

```
PdfExtractor (interface)
├── TextExtractor (PDFBox)
│   └── extracts: raw text + page boundaries
└── TableExtractor (Tabula-java)
    ├── extract(): List<List<String>>
    └── uses LATTICE mode only

TwoPhaseExtractor (implements PdfExtractor)
├── Phase 1: Extract all text + tables with page markers
│   └── Output: "<<<PAGE:N>>>" markers between pages
└── Phase 2: Parse with stateful context
```

### Table Extraction Strategy

- **Tabula-java with LATTICE mode only** - uses visible grid lines for cell detection
- No stream mode fallback - if LATTICE fails, fall back to PDFBox text extraction
- `ObjectExtractor.create(pdfDocument)` followed by `extract(ExtractionMethod.LATTICE)`

### Multi-Line Cell Handling

- Tabula-java returns cell text as single strings
- Post-processing: replace newline characters with spaces within cells
- Tabula's built-in text extraction handles concatenation of wrapped text

### Cell Misclassification Handling

**Problem:** Text from adjacent cells can be incorrectly classified when tables have unclear borders or irregular layouts.

**Solution:**

```
TableRefinementStrategy (interface)
├── HeuristicRefinement
│   ├── Checks cell content for expected patterns (e.g., attribute names)
│   ├── Identifies misclassified text by analyzing semantic patterns
│   └── Reassigns text to correct cells based on pattern matching
└── PositionalRefinement
    ├── Analyzes X/Y coordinates of text fragments
    ├── Compares with cell boundaries
    └── Reassigns fragments to nearest correct cell
```

**Misclassification Detection Heuristics:**

1. **Pattern-Based Detection:**
   - AUTOSAR attributes typically follow: `name : type [multiplicity]`
   - Flag cells with unexpected patterns (multiple attributes)

2. **Position-Based Correction:**
   - Use PDFBox `TextPosition` to get exact text coordinates
   - Compare with Tabula's cell boundaries
   - Reassign text fragments that fall outside expected cell bounds

3. **Content Validation:**
   - Known attribute type names (e.g., `Boolean`, `String`, `Integer`)
   - Known enumeration values
   - Flag cells with mixed content types

### Page Marker Format

`<<<PAGE:N>>>` markers inserted between pages to maintain state across page boundaries during Phase 2 parsing.

## Parsing Logic

### Two-Phase Parsing (Domain-First)

**Phase 1 - Extraction:**
- Raw text and table extraction with page boundaries
- Cell refinement applied during this phase
- Output: Complete text buffer with `<<<PAGE:N>>>` markers

**Phase 2 - Stateful Parsing:**

```
ParseContext (record)
├── currentPackage: Optional<AutosarPackageBuilder>
├── currentClass: Optional<AutosarClassBuilder>
├── currentPage: int
├── source: DocumentSource
└── packages: List<AutosarPackage>

SpecializedParsers:
├── ClassParser (recognizes class definitions)
├── EnumerationParser (recognizes enum tables)
└── PrimitiveParser (recognizes primitive types)
```

### Multi-Page Definition Handling

- Class definitions spanning multiple pages maintain context across `<<<PAGE:N>>>` markers
- Attribute tables parsed incrementally with current class context
- Parent-child relationships resolved after full parsing

### Pattern Recognition

- Class definition: `<Type> <Name>` with section headers
- Attributes: tables with Name/Type/Multiplicity columns
- Enumerations: labeled tables with literal values

## Output Layer

### Components

```
OutputWriter (interface)
├── MarkdownWriter
│   ├── writePackageHierarchy(Path): void
│   ├── writeClassDetails(AutosarClass): String
│   ├── writeMapping(Map<String, String>): String
│   └── writeInheritanceHierarchy(List<AutosarClass>): String
└── JsonWriter
    ├── writePackageHierarchy(Path): void
    ├── writeClassDetails(AutosarClass): JsonObject
    ├── writeMapping(Map<String, String>): JsonArray
    └── writeInheritanceHierarchy(List<AutosarClass>): JsonArray
```

### Output Formats

- Type-to-package mapping (Markdown or JSON)
- Class inheritance hierarchy (Markdown or JSON)
- Individual class details (Markdown or JSON, one file per class)

## CLI

### Command Structure

```
autosar-extract <pdf-files> [options]

Required:
  --mapping <file>         Generate type-to-package mapping
  --hierarchy <file>       Generate class inheritance hierarchy
  --class-details <dir>    Generate individual class files

Optional:
  --json                   Output in JSON format
  --markdown               Output in Markdown format
  -v, --verbose            Enable verbose output
  --log-file <file>        Write logs to file
```

### Validation

- At least one output flag must be specified
- Input PDF(s) must exist
- Output paths must be writable
- Cannot specify both `--json` and `--markdown`

### Format Auto-Detection

- `.md` → Markdown
- `.yaml`, `.yml` → YAML
- `.json` → JSON

### Example Usage

```bash
# Generate mapping, hierarchy, and class details
autosar-extract examples/pdf/ --mapping data/mapping.md --hierarchy data/hierarchy.md --class-details data/packages/

# JSON output only
autosar-extract examples/pdf/ --mapping data/mapping.json --json
```

## Build and Dependencies

### Build Tool

Maven - standard, widely used, XML configuration

### Key Dependencies

- **PDFBox 3.x** - PDF text extraction and position data
- **Tabula-java 2.x** - Table extraction with LATTICE mode
- **Jackson** - JSON serialization/deserialization
- **JCommander or Picocli** - CLI argument parsing

## Testing Strategy

### Test Types

- **Unit tests** - Individual component testing (parsers, writers)
- **Integration tests** - End-to-end PDF processing
- **Regression tests** - Compare against reference Python output

### Test Data

- Use existing AUTOSAR PDF examples from reference project
- Include edge case PDFs with complex table structures

## Implementation Plan

Next step: Invoke `writing-plans` skill to create detailed implementation tasks based on this design.

---

## References

- Reference Python implementation: `/Users/ray/Workspace/autosar-pdf/`
- AUTOSAR PDF examples: `/Users/ray/Workspace/autosar-pdf/examples/pdf/`
- Requirements docs from reference project
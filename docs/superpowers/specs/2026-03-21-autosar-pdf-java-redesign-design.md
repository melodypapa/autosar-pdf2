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

AutosarPackageBuilder (mutable builder)
├── name: String
├── path: List<String>
├── types: Map<String, AutosarTypeBuilder>
└── build(): AutosarPackage

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
- **Fallback behavior**: If LATTICE mode returns no tables on a page, fall back to PDFBox text extraction for that page
- `ObjectExtractor.create(pdfDocument)` followed by `extract(ExtractionMethod.LATTICE)`
- **No retries**: Single attempt per page, then fallback

### Multi-Line Cell Handling

- Tabula-java returns cell text as single strings
- Post-processing: replace newline characters with spaces within cells
- Tabula's built-in text extraction handles concatenation of wrapped text

### Cell Positional Refinement

**Problem:** Text from adjacent cells can be incorrectly classified when tables have unclear borders or irregular layouts.

**Solution:**

```
TableRefinementStrategy (interface)
├── refineCells(List<Cell>, List<TextPosition>): List<Cell>
└── PositionalRefinement (implementation)
    ├── Analyzes X/Y coordinates of text fragments
    ├── Compares with cell boundaries from Tabula
    └── Reassigns fragments to nearest correct cell
```

**Positional Refinement Algorithm:**
1. Extract text positions using PDFBox `TextPosition` for each page
2. Compare each text fragment's coordinates with Tabula's cell boundaries
3. If fragment falls outside expected cell bounds, reassign to nearest cell
4. Return refined cell content with corrected text assignments

**Refinement Invocation Criteria:**
- Positional refinement runs **always** during Phase 1 for pages with tables
- Applied after Tabula extraction but before text buffer assembly
- Skip refinement for pages where Tabula returned no tables (fallback to PDFBox)

**Integration:** Positional refinement runs during Phase 1 after Tabula extraction but before text buffer assembly. This keeps extraction focused on getting correct text positions without semantic analysis.

### Page Marker Format

`<<<PAGE:N>>>` markers inserted between pages to maintain state across page boundaries during Phase 2 parsing.

### Error Handling (Extraction Layer)

- **Corrupt PDF files:** Throw `PdfExtractionException` with details about the parsing error
- **PDFs with no visible grid lines:** LATTICE mode returns empty results, automatically falls back to PDFBox text extraction, logs warning
- **Memory issues with large PDFs:** Process pages sequentially with garbage collection hints, throw `OutOfMemoryError` with guidance on memory allocation
- **Missing PDF files:** Throw `FileNotFoundException` with input path details

## Parsing Logic

### Two-Phase Parsing (Domain-First)

**Phase 1 - Extraction:**
- Raw text and table extraction with page boundaries
- Positional refinement applied during this phase
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

### Parent Resolution Mechanism

**Resolution Order (after all parsing complete):**
1. For each `AutosarClass` with non-empty `bases` list:
   - For each parent name in `bases`:
     - Search for parent class across all packages (global lookup)
     - If found, set `parent` reference and add current class to parent's `subclasses`
     - If not found, log warning and leave `parent` as empty
2. Cross-package references are allowed - class names are globally scoped
3. Resolution order: iterate classes in parsed order to respect forward references

**Missing Parent Handling:**
- Log warning: "Parent class 'X' not found for class 'Y', reference will be unresolved"
- Continue parsing, leave `parent` field as `Optional.empty()`

### Pattern Recognition

**Class Definition Pattern:**
- Exact format: `[Aa]pplication(T|D)ype <Name>` where:
  - Type is `ApplicationType` or `ApplicationDataType` (case-insensitive)
  - Name follows after whitespace delimiter
- Section header identification: text preceded by blank line and newline, containing the class type pattern
- Distinguishing from other content: class definitions appear at section start (preceded by blank line + newline), not mid-paragraph

**Attribute Table Pattern:**
- Tables with column headers containing: "Name", "Type", "Multiplicity" (case-insensitive)
- Values follow standard AUTOSAR format: `name : type [multiplicity]`
- Tables appear following class definition sections

**Enumeration Table Pattern:**
- Tables with section headers containing "Enumeration" or "Literal" keywords
- Values are simple names or name-value pairs
- Tables appear in enumeration-specific sections

### Pattern-Based Validation (Parsing Phase)

**Validation rules applied during Phase 2 parsing:**

1. **Attribute Pattern Validation:**
   - Expected format: `name : type [multiplicity]`
   - Flag cells that don't match this pattern for review
   - Log warnings for suspected misclassifications

2. **Content Type Validation:**
   - Known attribute type names: `Boolean`, `String`, `Integer`, etc.
   - Known enumeration values (from parsed enums)
   - Flag cells with mixed content types for review

### Edge Case Handling

- **Empty PDF files:** Return empty `AutosarDocument` with warning log
- **PDFs with tables but no visible grid lines:** LATTICE fallback to PDFBox extraction, parser handles as plain text
- **Duplicate class names across different packages:** Allowed - namespaced by package path
- **Circular inheritance structures:** Detect during resolution phase, log error, skip circular references
- **Tables with inconsistent layouts:** Parser handles row-by-row, logs warnings for structure changes mid-table

### Error Handling (Parsing Logic)

- **Unrecognized patterns:** Skip with warning log, continue parsing
- **Circular inheritance:** Log error, break circular reference, continue
- **Duplicate type names in same package:** Log warning, use first occurrence

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

### JSON Output Schema

**AutosarPackage JSON:**
```json
{
  "name": "CommonStructure",
  "path": ["M2", "AUTOSAR", "CommonStructure"],
  "types": {
    "ReferrableClass": { /* AutosarClass schema */ },
    "SomeEnum": { /* AutosarEnumeration schema */ },
    "SomePrimitive": { /* AutosarPrimitive schema */ }
  },
  "source": {
    "filename": "AUTOSAR_CP_TPS_SystemTemplate.pdf",
    "page": 123,
    "standard": "AUTOSAR Classic Platform",
    "release": "R23-11"
  }
}
```

**AutosarClass JSON:**
```json
{
  "name": "ReferrableClass",
  "isAbstract": false,
  "atpType": "ApplicationType",
  "attributes": [
    {
      "name": "shortName",
      "type": "String",
      "defaultValue": null,
      "multiplicity": "1"
    }
  ],
  "bases": ["Identifiable"],
  "parent": null,
  "aggregatedBy": null,
  "subclasses": ["ReferrableClass2"]
}
```

**AutosarEnumeration JSON:**
```json
{
  "name": "YesOrNo",
  "literals": [
    { "name": "YES", "value": "true", "description": "Affirmative" },
    { "name": "NO", "value": "false", "description": "Negative" }
  ]
}
```

**AutosarPrimitive JSON:**
```json
{
  "name": "Boolean",
  "attributes": []
}
```

### Output Directory Structure

For `--class-details <dir>` option:
- Files organized by package hierarchy: `<dir>/<package_path>/<class_name>.<ext>`
- Example: `output/M2/AUTOSAR/CommonStructure/ReferrableClass.md`
- Missing directories are created automatically

### Output File Handling

- **Existing files:** Overwrite by default
- **Directory creation:** Create missing parent directories automatically
- **Write failures:** Throw `IOException` with path details, continue with remaining files

### Error Handling (Output Layer)

- **File write permissions:** Throw `IOException` with path details
- **JSON serialization errors:** Throw `JsonProcessingException` with details
- **Directory creation failures:** Throw `IOException` with path details

## CLI

### Command Structure

```
autosar-extract <pdf-files> [options]

Required (at least one):
  --mapping <file>         Generate type-to-package mapping
  --hierarchy <file>       Generate class inheritance hierarchy
  --class-details <dir>    Generate individual class files

Optional:
  --json                   Force JSON output format
  --markdown               Force Markdown output format
  -v, --verbose            Enable verbose output
  --log-file <file>        Write logs to file
```

### Format Handling

**Priority (highest to lowest):**
1. Explicit `--json` or `--markdown` flag overrides file extension
2. File extension determines format: `.md` → Markdown, `.json` → JSON
3. Default: Markdown

**Examples:**
```bash
# Explicit format flag takes precedence
autosar-extract input.pdf --mapping out.md --json        # Outputs JSON despite .md extension

# Extension-based auto-detection
autosar-extract input.pdf --mapping out.md               # Markdown
autosar-extract input.pdf --mapping out.json             # JSON
```

### Validation

- At least one output flag must be specified
- Cannot specify both `--json` and `--markdown` (mutually exclusive)
- Input PDF(s) must exist
- Output paths must be writable
- Output directories for `--class-details` are created if missing

### Example Usage

```bash
# Generate mapping, hierarchy, and class details (auto-detect format from extensions)
autosar-extract examples/pdf/ --mapping data/mapping.md --hierarchy data/hierarchy.md --class-details data/packages/

# Force JSON output for all outputs
autosar-extract examples/pdf/ --mapping data/mapping.json --hierarchy data/hierarchy.json --class-details data/packages/ --json

# Verbose mode with custom log file
autosar-extract input.pdf --mapping output.md -v --log-file extraction.log
```

### Error Handling (CLI)

- **No output flags specified:** Display help message, exit with error code
- **Invalid input paths:** Display error with invalid path, exit with error code
- **Mutually exclusive flags:** Display error message, exit with error code

## Build and Dependencies

### Build Tool

Maven - standard, widely used, XML configuration

### Key Dependencies

- **PDFBox 3.x** - PDF text extraction and position data
- **Tabula-java 2.x** - Table extraction with LATTICE mode
- **Jackson** - JSON serialization/deserialization
- **Picocli** - CLI argument parsing

## Testing Strategy

### Test Types

- **Unit tests** - Individual component testing (parsers, writers)
- **Integration tests** - End-to-end PDF processing
- **Regression tests** - Compare against reference Python output

### Test Data

- Use existing AUTOSAR PDF examples from reference project
- Include edge case PDFs with complex table structures

### Regression Testing

**Comparison Method:**
- Parse same PDFs with both Python and Java implementations
- Compare output structures (JSON format for easier comparison)
- Use JSON diff library to identify structural differences
- Semantic comparison: ignore whitespace differences, focus on content

**Which Outputs to Compare:**
- Type-to-package mappings (structural equivalence)
- Class inheritance hierarchies (parent-child relationships)
- Class details (attributes, enumerations, primitives)

**Acceptance Thresholds:**
- 100% match for type names and package paths
- 100% match for inheritance relationships
- 95%+ match for attribute extraction (allow for cell refinement differences)

**Failure Handling:**
- If attribute extraction falls below 95% match: test fails, differences logged with context (page number, cell location, expected vs actual)
- Full regression test suite must pass before considering implementation complete
- Individual test failures are reviewed and categorized as: implementation bugs vs. acceptable extraction differences

**Handling Format Differences:**
- Python outputs are used as reference expected values
- Java outputs are compared structurally, not textually
- Differences are logged with context (page, cell location) for investigation

## Implementation Plan

Next step: Invoke `writing-plans` skill to create detailed implementation tasks based on this design.

---

## References

- Reference Python implementation: `../autosar-pdf/`
- AUTOSAR PDF examples: `../autosar-pdf/examples/pdf/`
- Requirements docs from reference project: `../autosar-pdf/docs/requirements/`
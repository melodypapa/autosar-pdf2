# AUTOSAR PDF to Markdown/JSON - Java Implementation Design

**Date:** 2026-03-21
**Status:** Draft
**Approach:** Domain-First Redesign

## Overview

Redesign of AUTOSAR PDF extraction tool using Java, featuring improved table extraction with Tabula-java (LATTICE mode) and enhanced multi-line cell handling. This is a domain-first redesign based on AUTOSAR specification requirements rather than a direct port of the Python implementation.

## Architecture

### High-Level Data Flow

**For autosar-extract CLI (structured data extraction):**

```
AUTOSAR PDFs → PDF Extraction Layer → Domain Model → Output Layer → Markdown/JSON
                        ↓                        ↓
                    Tabula (tables)         Java Records/Classes
```

**For autosar-pdf2md CLI (direct PDF to Markdown):**

```
PDF → Tabula (tables) + PDFBox (text) → MarkdownConverter → Single Markdown File
```

### Layer Separation

1. **Extraction Layer** - PDFBox + Tabula for raw text and table extraction from PDFs
2. **Domain Layer** - Rich domain models representing AUTOSAR concepts (classes, enums, packages, attributes) - used by autosar-extract CLI only
3. **Output Layer** - Converters to Markdown and JSON formats

### Key Design Decisions

- **Java 17+ records** for immutable domain models
- **Functional-style parsing** where possible
- **Builder pattern** for complex object construction
- **Strategy pattern** for different parser types (class, enum, primitive)
- **Visitor pattern** for output generation
- **Separate CLI tools** - `autosar-extract` for structured extraction, `autosar-pdf2md` for direct format conversion

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

### Multi-Page Table Handling

**Problem:** Tables spanning multiple pages are extracted as separate table objects by Tabula, resulting in fragmented output.

**Detection Strategy:**
- Compare column count and column X-positions of consecutive tables on adjacent pages
- If column count matches AND column positions align (within tolerance threshold), tables are likely continuation of the same table
- Check for repeated header row on subsequent pages (common pattern in PDFs)

**Merging Algorithm:**
1. Extract all tables with their page numbers
2. Sort tables by page number and vertical position
3. For each table on page N:
   - Compare with tables on page N+1
   - If columns align and headers don't repeat, merge: append rows to page N table
   - If headers repeat on page N+1, treat as separate tables (headers indicate new table start)
4. Apply merged table data during Markdown generation or parsing

**MarkdownConverter Behavior (autosar-pdf2md CLI):**
- Multi-page tables merged into single Markdown table
- Page separator (`---`) inserted between merged table sections
- Row count annotation added: `<!-- Table continues across pages X-Y -->`

**TwoPhaseExtractor Behavior (autosar-extract CLI):**
- Multi-page attribute tables merged before stateful parsing
- Maintains current class context across page boundaries
- Parent-child resolution uses complete merged table data

**Fallback Behavior:**
- If table merging fails (inconsistent columns, conflicting data), log warning
- Output tables as separate entities per page
- No data loss - just fragmented representation

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

MarkdownConverter (for autosar-pdf2md CLI)
├── convertPdfToMarkdown(Path, ConversionOptions): String
├── extractTables(Path): List<MarkdownTable>
├── extractText(Path): List<MarkdownText>
└── combineContent(List<MarkdownTable>, List<MarkdownText>): String
```

**MarkdownTable (record):**
```
├── headers: List<String>
├── rows: List<List<String>>
├── pageNumber: int
└── cellAlignment: Optional<List<Alignment>>
```

**MarkdownText (record):**
```
├── content: String
├── pageNumber: int
├── position: TextPosition
└── type: TextType (HEADING, PARAGRAPH, LIST_ITEM)
```

**ConversionOptions (record):**
```
├── preserveTitles: boolean
├── tableOnly: boolean
├── insertPageBreaks: boolean
└── verbose: boolean
```

### Output Formats

**autosar-extract CLI:**
- Type-to-package mapping (Markdown or JSON)
- Class inheritance hierarchy (Markdown or JSON)
- Individual class details (Markdown or JSON, one file per class)

**autosar-pdf2md CLI:**
- Direct PDF to Markdown conversion (one PDF → one Markdown file)
- Tables converted to Markdown tables using Tabula
- All PDF content preserved in Markdown format

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

### autosar-pdf2md CLI

**Purpose:** Direct PDF to Markdown conversion - converts entire PDF content to a single Markdown file with tables preserved as Markdown tables.

**Command Structure:**

```
autosar-pdf2md <input-pdf> <output-md> [options]

Required:
  <input-pdf>             Path to input PDF file
  <output-md>             Path to output Markdown file

Optional:
  --preserve-titles       Preserve PDF section headers as Markdown headings
  --table-only            Extract only tables, skip text content
  -v, --verbose           Enable verbose output
  --log-file <file>       Write logs to file
```

**Conversion Strategy:**

```
PDF → Tabula (LATTICE mode) + PDFBox (text) → Markdown Generator → Single .md file
```

**Extraction Process:**

1. **Page-by-page processing:**
   - Use Tabula LATTICE mode to extract tables
   - Use PDFBox to extract non-table text with position data

2. **Table detection:**
   - If Tabula detects tables on a page, convert to Markdown tables
   - If no tables detected, extract text with PDFBox

3. **Markdown generation:**
   - Tables: Convert to Markdown format with proper column alignment
   - Text: Convert paragraphs with appropriate spacing
   - Section headers: Detect and convert to Markdown headings (H1-H6)
   - Page breaks: Insert horizontal rule `---` between pages

4. **Multi-line cell handling:**
   - Replace newlines within cells with spaces
   - Preserve cell content structure

**Example Usage:**

```bash
# Simple PDF to Markdown conversion
autosar-pdf2md input.pdf output.md

# With section headers preserved
autosar-pdf2md input.pdf output.md --preserve-titles

# Extract only tables
autosar-pdf2md input.pdf tables.md --table-only

# Verbose mode with logging
autosar-pdf2md input.pdf output.md -v --log-file conversion.log
```

**Validation:**

- Input PDF must exist
- Output file path must be writable
- Parent directories created automatically if missing

**Error Handling (autosar-pdf2md CLI):**

- **Corrupt PDF files:** Throw `PdfExtractionException` with details
- **No tables detected:** Log warning, continue with text extraction
- **File write failures:** Throw `IOException` with path details

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

### Python Integration Test Compatibility

**Critical Requirement:** The Java implementation must pass all integration tests from the Python reference implementation.

**Test Coverage:**
The Python integration tests (`tests/integration/test_pdf_integration.py`) cover 11 major test cases with complex edge cases:

| Test Case ID | Description | Key Edge Cases |
|--------------|-------------|----------------|
| SWIT_00001 | AUTOSAR, SwComponentType, ARElement verification | Multi-page class definitions, base class parsing, note extraction, source tracking |
| SWIT_00002 | TimingExtensions class list verification | 148 expected classes, complete extraction validation |
| SWIT_00003 | AtomicSwComponentType base classes | Base class extraction, ATP interface separation (bases vs implements) |
| SWIT_00004 | DiagnosticDebounceBehaviorEnum | Enumeration literal extraction, multi-line descriptions, immutability |
| SWIT_00005 | Enumeration literal tags extraction | Tag parsing (atp.EnumerationLiteralIndex, xml.name), description cleaning |
| SWIT_00006 | Multi-page enumeration literal list | ByteOrderEnum across multiple pages, literal structure validation |
| SWIT_00007 | Stacked literal names (enum3.png) | Three literal names in one cell combined into one literal |
| SWIT_00008 | Pattern 5 enumeration behavior | Same base name with different suffixes as separate literals |
| SWIT_00009 | Total counts and sorted lists | 4 PDFs, class/enumeration/primitive counts, alphabetical sorting |
| SWIT_00010 | Hyphenated attribute continuation | J1939Cluster request2Support from "re-" + "quest2Support" |
| SWIT_00012 | CamelCase fragment attributes | bswModuleDocumentation from "bswModule" + "Documentation" |

**Edge Case Handling Requirements:**

1. **Multi-line attribute parsing:**
   - Attribute names split across PDF lines must be concatenated
   - Hyphenated word breaks (e.g., "re-" + "quest2Support" → "request2Support")
   - CamelCase fragments (e.g., "bswModule" + "Documentation" → "bswModuleDocumentation")
   - Attribute types split across lines (e.g., "SwComponent" + "Documentation" → "SwComponentDocumentation")

2. **Enumeration literal handling:**
   - Multi-page literal lists (maintain context across page boundaries)
   - Stacked literal names in single cell (combine into one literal name)
   - Tag extraction (atp.EnumerationLiteralIndex, xml.name)
   - Multi-line descriptions for literals
   - Immutability enforcement (tuple for literals)

3. **Base class and ATP interface parsing:**
   - Distinguish between regular bases and ATP interfaces
   - ATP interfaces go into `implements` field, not `bases`
   - Base class corruption detection (e.g., prevent "SwComponentTypeClass AtomicSwComponentType (abstract)")

4. **Source location tracking:**
   - PDF file name
   - Page number
   - AUTOSAR standard and release information
   - Track for all types (classes, enumerations, primitives)

**Test Execution Strategy:**
- Java tests mirror Python test structure and assertions
- Test fixtures: session-scoped for performance (same PDF parsed once per test session)
- Expected counts: minimum thresholds to allow for future PDF version changes
- Same PDF files used as reference: `../autosar-pdf/examples/pdf/`

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

**Python Integration Test Compatibility:**
- All 11 Python integration tests (SWIT_00001-SWIT_00012) must pass in Java implementation
- Test assertions must match Python test expectations exactly
- Edge case handling (multi-line attributes, camelCase fragments, hyphenated breaks) must match Python behavior
- Output format (JSON/Markdown) must be structurally equivalent to Python output

**Failure Handling:**
- If attribute extraction falls below 95% match: test fails, differences logged with context (page number, cell location, expected vs actual)
- If any Python integration test fails: implementation not complete
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

- Reference Python implementation: `docs/reference/` (copied from original for reference only)
- AUTOSAR PDF examples: `examples/pdf/` (copied from original)
- Requirements docs: `docs/reference/docs/requirements/` (copied from original)
- Python integration tests: `tests/reference/integration/test_pdf_integration.py` (must all pass)
- Test case files: `tests/reference/integration/timing_extensions_class_list.txt` (expected class list)
- Test PDFs for edge cases: `examples/pdf/` (all PDF files copied from original)
- Development documentation: `docs/reference/docs/development/`
- Test cases documentation: `docs/reference/docs/test_cases/`
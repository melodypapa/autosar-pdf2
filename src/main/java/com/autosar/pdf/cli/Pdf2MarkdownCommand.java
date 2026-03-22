package com.autosar.pdf.cli;

import com.autosar.pdf.writer.MarkdownConverter;
import com.autosar.pdf.writer.models.ConversionOptions;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * Command-line interface for converting PDF documents to Markdown format.
 *
 * This command converts entire PDF content to a single Markdown file with
 * tables preserved as Markdown tables.
 */
@Command(name = "pdf2md", mixinStandardHelpOptions = true,
        description = "Convert PDF documents to Markdown format with preserved tables")
public class Pdf2MarkdownCommand implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "Input PDF file")
    public Path inputPdf;

    @CommandLine.Parameters(index = "1", description = "Output Markdown file")
    public Path outputMd;

    @Option(names = {"--preserve-titles"}, description = "Preserve PDF section headers as Markdown headings")
    public boolean preserveTitles = false;

    @Option(names = {"--table-only"}, description = "Extract only tables, skip text content")
    public boolean tableOnly = false;

    @Option(names = {"-v", "--verbose"}, description = "Enable verbose output")
    public boolean verbose = false;

    @Option(names = {"--log-file"}, description = "Write logs to specified file")
    public Path logFile;

    @Override
    public Integer call() throws Exception {
        if (inputPdf == null || !Files.exists(inputPdf)) {
            System.err.println("Error: Input PDF file not found: " + inputPdf);
            return 1;
        }

        // Create output directory if it doesn't exist
        Path outputParent = outputMd.getParent();
        if (outputParent != null && !Files.exists(outputParent)) {
            Files.createDirectories(outputParent);
        }

        MarkdownConverter converter = new MarkdownConverter();
        ConversionOptions options = new ConversionOptions(
            preserveTitles, tableOnly, true, verbose
        );

        String markdown = converter.convertPdfToMarkdown(inputPdf, options);
        Files.writeString(outputMd, markdown);

        if (verbose) {
            System.out.println("Successfully converted " + inputPdf + " to " + outputMd);
        }

        return 0;
    }
}
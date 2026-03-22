package com.autosar.pdf;

import picocli.CommandLine;
import com.autosar.pdf.cli.AutosarExtractCommand;
import com.autosar.pdf.cli.Pdf2MarkdownCommand;

/**
 * Main entry point for the AUTOSAR PDF to Markdown/JSON tool.
 *
 * This tool provides two commands:
 * - autosar-extract: Extract structured AUTOSAR data from PDFs
 * - pdf2md: Convert PDF documents directly to Markdown format
 */
public class Main {

    /**
     * Executes the application with the given arguments.
     *
     * @param args Command-line arguments
     * @return Exit code (0 for success, non-zero for error)
     */
    public static int execute(String[] args) {
        if (args.length > 0 && args[0].equals("pdf2md")) {
            Pdf2MarkdownCommand cmd = new Pdf2MarkdownCommand();
            String[] shifted = shiftArgs(args);
            return new CommandLine(cmd).execute(shifted);
        } else {
            AutosarExtractCommand cmd = new AutosarExtractCommand();
            return new CommandLine(cmd).execute(args);
        }
    }

    public static void main(String[] args) {
        System.exit(execute(args));
    }

    /**
     * Removes the first argument from the array (used for subcommand handling).
     *
     * @param args Original arguments array
     * @return Array without the first argument
     */
    private static String[] shiftArgs(String[] args) {
        if (args.length <= 1) return new String[0];
        String[] shifted = new String[args.length - 1];
        System.arraycopy(args, 1, shifted, 0, args.length - 1);
        return shifted;
    }
}
package org.basex.query.parse;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * Regenerates the REx parser: fetches the current grammar from its home, adds the rule for the
 * entry point {@code SequenceTypeEOF}, notes the commit in its header comment, stores it next to
 * the test resources, and invokes {@code rex -remake XQuerySyntaxParser.xml} there, which
 * regenerates the XML representation of the grammar. The parser is regenerated with
 * {@code rex -remake XQuerySyntaxParser.java} in its own directory, below {@code src/main}: the
 * command line recorded in its header refers to the grammar by a relative path. The line endings
 * of the results are those of the system. This is a main program, not a test; it is the first
 * step of {@link GrammarUpdate}.
 *
 * <p>The grammar is always fetched, as of the latest commit that changed it. REx (which must be
 * on the path) always runs.</p>
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class RExRemake {
  /** Repository of the grammar. */
  private static final String REPO = "GuntherRademacher/rex-parser-generator";
  /** Path of the grammar in the repository. */
  private static final String GRAMMAR =
      "docs/sample-grammars/XQuery-Full-Text-Update-BaseX-40.ebnf";
  /** End of the header comment of the grammar. */
  private static final String HEADER_END = "\n */";
  /** Commit hash in the response of the GitHub API. */
  private static final Pattern SHA = Pattern.compile("\"sha\"\\s*:\\s*\"([0-9a-f]{40})\"");
  /** Directory of the generated parser. */
  private static final Path DIR = Path.of("src/main/java/org/basex/query/parse/gen");
  /** Directory of the grammar and its XML form, which are needed by the tests only. */
  private static final Path RESOURCES = Path.of("src/test/resources/org/basex/query/parse");
  /** Marker of the line that precedes the token definitions. */
  private static final String TOKENS = "<?TOKENS?>";
  /** Name of the rule to be added. */
  private static final String ENTRY = "SequenceTypeEOF";
  /** Rule to be added. */
  private static final String RULE = ENTRY + "\n         ::= SequenceType EOF\n\n";

  /** Private constructor. */
  private RExRemake() { }

  /**
   * Main method.
   * @param args ignored; to be started in the directory of module basex-core, or its parent
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    remake(Files.isDirectory(DIR) ? Path.of(".") : Path.of("basex-core"));
  }

  /**
   * Fetches the grammar, and regenerates the parser.
   * @param core directory of module basex-core
   * @throws Exception exception
   */
  static void remake(final Path core) throws Exception {
    final Path parser = core.resolve(DIR), resources = core.resolve(RESOURCES);
    for(final Path dir : List.of(parser, resources)) {
      if(!Files.isDirectory(dir)) throw new IOException("Directory not found: " + dir);
    }
    final Path grammarFile = resources.resolve(GRAMMAR.substring(GRAMMAR.lastIndexOf('/') + 1));

    // fetch the grammar as of the latest commit that changed it
    try(HttpClient client = HttpClient.newHttpClient()) {
      // the first sha of the response is the one of the latest commit
      final Matcher latest = SHA.matcher(fetch(client, "https://api.github.com/repos/" + REPO +
          "/commits?sha=main&per_page=1&path=" + GRAMMAR));
      if(!latest.find()) throw new IOException("No commit found for " + GRAMMAR);
      final String commit = latest.group(1);
      write(grammarFile, grammar(fetch(client, "https://raw.githubusercontent.com/" + REPO + '/' +
          commit + '/' + GRAMMAR), commit));
      log("Grammar fetched: commit " + commit + '.');
    }

    // regenerate the parser and its XML representation, with the line endings of the system
    log("Running REx.");
    for(final Path file : List.of(resources.resolve("XQuerySyntaxParser.xml"),
        parser.resolve("XQuerySyntaxParser.java"))) {
      rex(file.getParent(), "-remake", file.getFileName().toString());
      write(file, Files.readString(file, StandardCharsets.UTF_8));
    }
  }

  /**
   * Prepares the grammar: notes the commit in the header comment, and adds the rule before the
   * line that contains the marker.
   * @param body grammar as fetched
   * @param commit commit
   * @return grammar
   * @throws IOException I/O exception
   */
  private static String grammar(final String body, final String commit)
      throws IOException {

    final String text = body.replace("\r\n", "\n");
    final int end = text.indexOf(HEADER_END);
    if(end == -1) throw new IOException("Header comment not found.");
    final String noted = text.substring(0, end + 1) + " * fetched from https://github.com/" + REPO +
        ", commit " + commit + "\n * extended by RExRemake with the rule " + ENTRY +
        text.substring(end);
    final int marker = noted.indexOf(TOKENS);
    if(marker == -1) throw new IOException("Marker not found: " + TOKENS);
    final int line = noted.lastIndexOf('\n', marker) + 1;
    return noted.substring(0, line) + RULE + noted.substring(line);
  }

  /**
   * Writes a notice.
   * @param message message
   */
  private static void log(final String message) {
    System.out.println("RExRemake: " + message);
  }

  /**
   * Writes a file with the line endings of the system.
   * @param file file
   * @param text text, with any line endings
   * @throws IOException I/O exception
   */
  private static void write(final Path file, final String text) throws IOException {
    Files.writeString(file, text.replace("\r\n", "\n").replace("\n", System.lineSeparator()),
        StandardCharsets.UTF_8);
  }

  /**
   * Fetches a text.
   * @param client HTTP client
   * @param url location
   * @return text
   * @throws Exception exception
   */
  private static String fetch(final HttpClient client, final String url) throws Exception {
    final HttpResponse<String> response = client.send(HttpRequest.newBuilder(URI.create(url)).
        build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    if(response.statusCode() != 200) {
      throw new IOException("HTTP " + response.statusCode() + ": " + url);
    }
    return response.body();
  }

  /**
   * Runs REx.
   * @param dir working directory
   * @param args arguments
   * @throws Exception exception
   */
  private static void rex(final Path dir, final String... args) throws Exception {
    final List<String> command = new ArrayList<>();
    command.add("rex");
    command.addAll(List.of(args));
    final Process process;
    try {
      process = new ProcessBuilder(command).directory(dir.toFile()).inheritIO().start();
    } catch(final IOException ex) {
      throw new IOException("REx cannot be started: is it on the path? The grammar is " +
          "up to date. Run again when REx is available.", ex);
    }
    final int exit = process.waitFor();
    if(exit != 0) throw new IOException("REx exited with code " + exit + ": " + command);
  }
}

package org.basex.query.parse;

import static org.basex.util.Token.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

import javax.xml.parsers.*;

import org.basex.core.*;
import org.basex.core.MainOptions.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.w3c.dom.*;

/**
 * Compares the query parsers on the queries of the QT4 test suite, which is taken from the
 * environment variable {@code QT4_PATH}, or from a {@code qt4tests} directory next to the root
 * project: can they be parsed, and if so, is the plan the same; if not, are the error code,
 * position and message the same? Queries are only parsed, but neither compiled nor run: this tells
 * how the parsers differ, not whether the results are right. Run the main method; it asserts
 * nothing.
 *
 * <p>The optional argument is a report file. {@code MANUAL} is the reference, and {@code REX} is
 * compared to it. It takes about a minute. The result is a table with the number of queries per
 * class of outcome, and a verdict for each class:</p>
 * <ul>
 *   <li>{@code same}: nothing to see. {@code parsed}: both parsers parse the query and build the
 *     same plan; {@code failed}: both fail with the same error code, position and message (many
 *     queries are meant to fail).</li>
 *   <li>{@code tolerated}: a difference that we have agreed to live with. The wording and position
 *     of syntax errors depend on the parser; and the generated parser parses a query completely
 *     before checking it, so it may report a syntax error at the same position or further on
 *     where the hand-written one has reported a semantic error before.</li>
 *   <li>{@code REVIEW}: everything else. Semantic errors are to be reported with the same code
 *     and position, and the plans are to be the same. Differences are to be fixed, or explained
 *     by the grammar: some queries are syntax errors for the grammar where the hand-written
 *     parser reports a specific error (validate expressions, schema-element and schema-attribute
 *     tests, cast to function(*)), or vice versa.</li>
 * </ul>
 *
 * <p>A report file is a Markdown document: the table, and then the queries of all classes that
 * are not {@code same}, with the results of both parsers, the classes to be reviewed first.
 * Unreadable test sets and missing query files are reported, as runs with different inputs are
 * not comparable.</p>
 *
 * <p>This class is only useful as long as there are two parsers: remove it together with
 * {@code QueryParser}.</p>
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class ParseDiff {
  /** Namespace of the test catalog. */
  private static final String NS = "http://www.w3.org/2010/09/qt-fots-catalog";
  /** Error code of syntax errors. */
  private static final String SYNTAX = "XPST0003";

  /**
   * Outcome of a parser.
   * @param error indicates if parsing the query failed
   * @param code error code
   * @param line line of the error
   * @param column column of the error
   * @param message error message
   * @param plan plan of a query that was parsed
   */
  private record Outcome(boolean error, String code, int line, int column, String message,
      String plan) { }

  /** Reference parser. */
  private static final QueryParserType REF = QueryParserType.MANUAL;
  /** Parser that is compared to the reference. */
  private static final QueryParserType OTHER = QueryParserType.REX;
  /** Pattern of the error codes of two parsers at the end of a class. */
  private static final String CODES =
      ": [A-Z]{4}[0-9]{4} \\(\\w+\\) / [A-Z]{4}[0-9]{4} \\(\\w+\\)$";
  /** Verdict: nothing to see. */
  private static final String SAME = "same";
  /** Verdict: a difference that is tolerated. */
  private static final String TOLERATED = "tolerated";
  /** Verdict: a difference to be fixed or explained. */
  private static final String REVIEW = "REVIEW";
  /** Maximum number of names that are reported for skipped inputs. */
  private static final int MAX_NAMES = 10;

  /**
   * Queries of a test suite, and the inputs that could not be used.
   * @param queries queries by name of the test case
   * @param skippedSets test sets that could not be read
   * @param missingFiles query files that do not exist
   */
  private record Corpus(Map<String, String> queries, List<String> skippedSets,
      List<String> missingFiles) { }

  /** Private constructor. */
  private ParseDiff() { }

  /**
   * Main method.
   * @param args report file (optional)
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    final Path suite = QT4Suite.find();
    final QueryParserType ref = REF, other = OTHER;
    try (PrintStream report =
        args.length > 0 ? new PrintStream(args[0], StandardCharsets.UTF_8) : null) {

      final Corpus corpus = corpus(suite);
      final Map<String, String> queries = corpus.queries;
      final Map<String, Integer> counts = new TreeMap<>();
      final Map<String, List<String>> samples = new TreeMap<>();
      final Context context = new Context();
      try {
        for(final Map.Entry<String, String> entry : queries.entrySet()) {
          final String query = entry.getValue();
          final Outcome a = outcome(context, ref, query), b = outcome(context, other, query);
          final String type = type(a, b);
          counts.merge(type, 1, Integer::sum);
          if(report != null && !type.startsWith("same")) {
            samples.computeIfAbsent(type, k -> new ArrayList<>()).add(sample(entry.getKey(), query,
                ref, a, other, b));
          }
        }
      } finally {
        context.close();
      }

      final String summary = summary(queries.size(), ref, other, corpus, counts, report != null);
      System.out.print(summary);
      if(report != null) {
        report.print(summary);
        for(final String verdict : List.of(REVIEW, TOLERATED)) {
          for(final Map.Entry<String, List<String>> entry : samples.entrySet()) {
            final String type = entry.getKey();
            if(!verdict(type).equals(verdict)) continue;
            report.println("\n## `" + type + "`: " + entry.getValue().size() + " queries, " +
                verdict + "\n\n" + description(type) + '\n');
            entry.getValue().forEach(report::println);
          }
        }
      }
    }
  }

  /**
   * Creates the summary: a table with the classes of outcomes, and a description of these.
   * @param size number of queries
   * @param ref reference parser
   * @param other other parser
   * @param corpus queries and skipped inputs
   * @param counts number of queries by class
   * @param details indicates if the queries of the classes are written to a report file
   * @return summary
   */
  private static String summary(final int size, final QueryParserType ref,
      final QueryParserType other, final Corpus corpus, final Map<String, Integer> counts,
      final boolean details) {

    final String nl = System.lineSeparator();
    final StringBuilder sb = new StringBuilder(size + " queries: " + ref + " (reference) and " +
        other + nl + nl);
    report(sb, "test sets skipped", corpus.skippedSets);
    report(sb, "query files missing", corpus.missingFiles);

    // verdicts are shown in the order of their importance
    final Map<String, Integer> verdicts = new LinkedHashMap<>();
    for(final String verdict : List.of(REVIEW, TOLERATED, SAME)) verdicts.put(verdict, 0);
    final List<String[]> rows = new ArrayList<>();
    for(final String verdict : verdicts.keySet()) {
      for(final Map.Entry<String, Integer> entry : counts.entrySet()) {
        if(!verdict(entry.getKey()).equals(verdict)) continue;
        rows.add(new String[] { verdict, Integer.toString(entry.getValue()), entry.getKey() });
        verdicts.merge(verdict, entry.getValue(), Integer::sum);
      }
    }
    sb.append(nl).append(MarkdownTable.format(new String[] { "verdict", "queries", "class" },
        rows.toArray(String[][]::new), new boolean[] { true, false, true }));

    sb.append(nl).append(verdicts.get(REVIEW) == 0 ? "Nothing to review." :
      verdicts.get(REVIEW) + " queries are to be reviewed" +
      (details ? "; see the report file." : ". Specify a report file to see them.")).append(nl);

    // describe the classes that occurred
    sb.append(nl).append("The classes:").append(nl);
    final Set<String> described = new HashSet<>();
    for(final String type : counts.keySet()) {
      final String text = description(type);
      if(described.add(text)) sb.append("- `").append(type.replaceAll(CODES, "")).append("`: ").
        append(text).append(nl);
    }
    return sb.toString();
  }

  /**
   * Returns the verdict for a class of outcomes.
   * @param type class
   * @return {@link #SAME}, {@link #TOLERATED} or {@link #REVIEW}
   */
  private static String verdict(final String type) {
    if(type.startsWith("same")) return SAME;
    if(type.startsWith("ACCEPT") || type.equals("DIFF position: syntax error") ||
        type.equals("DIFF message: syntax error")) return TOLERATED;
    return REVIEW;
  }

  /**
   * Describes a class of outcomes.
   * @param type class
   * @return description
   */
  private static String description(final String type) {
    if(type.equals("same: parsed")) return "both parsers parse the query successfully and build " +
        "the same plan.";
    if(type.equals("same: failed")) return "both parsers fail with the same error code, position " +
        "and message.";
    if(type.startsWith("ACCEPT later syntax error")) return "the reference parser reports a " +
        "semantic error, the other a syntax error at the same position or further on, as it " +
        "parses the whole query before it checks anything.";
    if(type.equals("DIFF position: syntax error")) return "syntax error with the same code, " +
        "reported at a different position.";
    if(type.equals("DIFF message: syntax error")) return "syntax error with the same code and " +
        "position, but a different message.";
    if(type.startsWith("DIFF code")) return "both fail, with different error codes " +
        "(each is followed by the parser that reports it). Differences that the grammar causes " +
        "are known; check for others.";
    if(type.startsWith("DIFF position")) return "error of the same code, reported at a " +
        "different position: to be fixed, as semantic errors are to be reported at the same " +
        "position.";
    if(type.startsWith("DIFF message")) return "error with the same code and position, but a " +
        "different message.";
    if(type.equals("DIFF plan")) return "both parsers parse the query, but build different " +
        "plans.";
    return "one parser parses the query, and the other fails (the error code is shown).";
  }

  /**
   * Reports inputs that were skipped.
   * @param sb string builder
   * @param label description
   * @param names names of the inputs
   */
  private static void report(final StringBuilder sb, final String label,
      final List<String> names) {
    sb.append("- ").append(label).append(": ").append(names.size());
    final int size = Math.min(names.size(), MAX_NAMES);
    if(size > 0) {
      sb.append(" (").append(String.join(", ", names.subList(0, size)));
      if(names.size() > size) sb.append(", ...");
      sb.append(')');
    }
    sb.append('\n');
  }

  /**
   * Parses a query.
   * @param context database context
   * @param type parser
   * @param query query
   * @return outcome
   */
  private static Outcome outcome(final Context context, final QueryParserType type,
      final String query) {

    try(QueryContext qc = new QueryContext(context)) {
      qc.queryParser = type;
      qc.parse(query, null);
      // the query text of a module does not show library functions: compare the plan
      // XML 1.1 can represent all control characters of the queries
      final SerializerOptions options = new SerializerOptions();
      options.set(SerializerOptions.VERSION, "1.1");
      return new Outcome(false, "", 0, 0, "", qc.toXml(false).serialize(options).toString());
    } catch(final QueryIOException ex) {
      throw new IllegalStateException(ex);
    } catch(final QueryException ex) {
      return new Outcome(true, string(ex.qname().local()), ex.line(), ex.column(),
          ex.getMessage().replaceAll("\\s+", " "), "");
    }
  }

  /**
   * Classifies the outcomes of two parsers.
   * @param a outcome of the reference parser
   * @param b outcome of the other parser
   * @return class
   */
  private static String type(final Outcome a, final Outcome b) {
    if(!a.error && !b.error) return a.plan.equals(b.plan) ? "same: parsed" : "DIFF plan";
    if(!a.error) return "DIFF " + REF + " parses, " + OTHER + " fails: " + b.code;
    if(!b.error) return "DIFF " + REF + " fails: " + a.code + ", " + OTHER + " parses";
    if(!a.code.equals(b.code)) {
      final boolean later = b.code.equals(SYNTAX) && (b.line > a.line ||
          b.line == a.line && b.column >= a.column);
      return (later ? "ACCEPT later syntax error: " : "DIFF code: ") + a.code + " (" + REF +
          ") / " + b.code + " (" + OTHER + ')';
    }
    if(a.line != b.line || a.column != b.column) {
      return "DIFF position" + (a.code.equals(SYNTAX) ? ": syntax error" : ": other error");
    }
    if(!a.message.equals(b.message)) {
      return "DIFF message" + (a.code.equals(SYNTAX) ? ": syntax error" : ": other error");
    }
    return "same: failed";
  }

  /**
   * Describes the outcome of a parser.
   * @param outcome outcome
   * @return description
   */
  private static String describe(final Outcome outcome) {
    return outcome.error ? outcome.code + ' ' + outcome.line + '/' + outcome.column + ' ' +
        outcome.message : "parsed";
  }

  /**
   * Describes a query for the report.
   * @param name name of the test case
   * @param query query
   * @param ref reference parser
   * @param a outcome of the reference parser
   * @param other other parser
   * @param b outcome of the other parser
   * @return description
   */
  private static String sample(final String name, final String query, final QueryParserType ref,
      final Outcome a, final QueryParserType other, final Outcome b) {

    // labels of the lines, with the values to be shown
    final List<String[]> lines = new ArrayList<>();
    lines.add(new String[] { "query", query.replaceAll("\\s+", " ").trim() });
    lines.add(new String[] { ref.toString(), describe(a) });
    lines.add(new String[] { other.toString(), describe(b) });
    if(!a.error && !b.error) {
      lines.add(new String[] { "plan " + ref, a.plan });
      lines.add(new String[] { "plan " + other, b.plan });
    }
    int width = 0;
    for(final String[] line : lines) width = Math.max(width, line[0].length());

    // the outcomes are shown as code, as queries and plans contain Markdown and XML syntax
    final StringBuilder sb = new StringBuilder("### `" + name + "`\n\n```\n");
    for(final String[] line : lines) {
      sb.append(String.format("%-" + (width + 1) + "s", line[0] + ':')).append(' ').append(line[1]).
        append('\n');
    }
    sb.append("```\n");
    return sb.toString();
  }

  /**
   * Collects the queries of a test suite.
   * @param dir directory of the test suite
   * @return queries, and the inputs that could not be used
   * @throws Exception exception
   */
  private static Corpus corpus(final Path dir) throws Exception {
    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    final DocumentBuilder db = dbf.newDocumentBuilder();

    final Map<String, String> queries = new LinkedHashMap<>();
    final List<String> skipped = new ArrayList<>(), missing = new ArrayList<>();
    final NodeList sets = db.parse(dir.resolve("catalog.xml").toFile()).
        getElementsByTagNameNS(NS, "test-set");
    for(int s = 0; s < sets.getLength(); s++) {
      final String setName = ((Element) sets.item(s)).getAttribute("file");
      final Path file = dir.resolve(setName);
      final Document set;
      try {
        set = db.parse(file.toFile());
      } catch(final IOException | org.xml.sax.SAXException ex) {
        skipped.add(setName);
        continue;
      }
      final NodeList tests = set.getElementsByTagNameNS(NS, "test-case");
      for(int t = 0; t < tests.getLength(); t++) {
        final Element tc = (Element) tests.item(t);
        final NodeList list = tc.getElementsByTagNameNS(NS, "test");
        if(list.getLength() == 0) continue;
        final Element test = (Element) list.item(0);
        final String name = file.getFileName() + ": " + tc.getAttribute("name");
        if(!test.hasAttribute("file")) {
          queries.put(name, test.getTextContent());
        } else {
          final Path query = file.resolveSibling(test.getAttribute("file"));
          if(Files.exists(query)) {
            queries.put(name, Files.readString(query, StandardCharsets.UTF_8));
          } else {
            missing.add(dir.relativize(query).toString().replace('\\', '/'));
          }
        }
      }
    }
    return new Corpus(queries, skipped, missing);
  }
}

package org.basex.query.parse;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

import javax.xml.parsers.*;

import org.basex.core.*;
import org.basex.core.MainOptions.*;
import org.basex.query.*;
import org.w3c.dom.*;

/**
 * Measures the time that the query parsers need for the queries of the QT4 test suite, which is
 * taken from the environment variable {@code QT4_PATH}, or from a {@code qt4tests} directory next
 * to the root project. The times are printed, not asserted. Progress is reported after each round,
 * outside of the timed region, so that a profiler can be attached after the warm-up. A Markdown
 * table shows all rounds, and the best time and the median of the measurement rounds (those after
 * the warm-up).
 *
 * Arguments (optional): the number of rounds (default: 6) and of warm-up rounds (default: 2).
 * Both parsers are measured. Unreadable test sets and missing query files are reported, as runs
 * with different inputs are not comparable. Queries that raise errors are parsed as well, so the
 * time includes the creation of the errors.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class ParseBench {
  /** Namespace of the test catalog. */
  private static final String NS = "http://www.w3.org/2010/09/qt-fots-catalog";
  /** Maximum number of names that are reported for skipped inputs. */
  private static final int MAX_NAMES = 10;

  /**
   * Queries of a test suite, and the inputs that could not be used.
   * @param queries queries
   * @param skippedSets test sets that could not be read
   * @param missingFiles query files that do not exist
   */
  private record Corpus(List<String> queries, List<String> skippedSets,
      List<String> missingFiles) { }

  /** Private constructor. */
  private ParseBench() { }

  /**
   * Main method.
   * @param args rounds, warm-up rounds (all optional)
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    final Corpus corpus = corpus(QT4Suite.find());
    final List<String> queries = corpus.queries;
    final QueryParserType[] types = QueryParserType.values();
    final int rounds = args.length > 0 ? Integer.parseInt(args[0]) : 6;
    final int warmup = Math.min(rounds - 1, args.length > 1 ? Integer.parseInt(args[1]) : 2);
    System.out.println(queries.size() + " queries");
    report("test sets skipped", corpus.skippedSets);
    report("query files missing", corpus.missingFiles);

    final long[][] times = new long[rounds][types.length];
    final int[] accepted = new int[types.length];
    final Context context = new Context();
    try {
      for(int round = 0; round < rounds; round++) {
        for(int t = 0; t < types.length; t++) {
          int ok = 0;
          final long start = System.nanoTime();
          for(final String query : queries) {
            try(QueryContext qc = new QueryContext(context)) {
              qc.queryParser = types[t];
              qc.parse(query, null);
              ok++;
            } catch(final QueryException ex) {
              // errors are expected
            }
          }
          times[round][t] = (System.nanoTime() - start) / 1_000_000;
          accepted[t] = ok;
        }
        // progress, after the measurement
        final StringBuilder sb = new StringBuilder("round " + (round + 1) + (round < warmup ?
            " (warm-up)" : "") + ":");
        for(int t = 0; t < types.length; t++) {
          sb.append("  ").append(types[t]).append(' ').append(times[round][t]).append(" ms");
        }
        System.out.println(sb);
      }
    } finally {
      context.close();
    }
    System.out.println();
    for(int t = 0; t < types.length; t++) {
      System.out.println(types[t] + ": " + accepted[t] + " queries without errors");
    }
    System.out.println();
    table(types, times, warmup);
  }

  /**
   * Reports inputs that were skipped.
   * @param label description
   * @param names names of the inputs
   */
  private static void report(final String label, final List<String> names) {
    final StringBuilder sb = new StringBuilder(label + ": " + names.size());
    final int size = Math.min(names.size(), MAX_NAMES);
    if(size > 0) {
      sb.append(" (").append(String.join(", ", names.subList(0, size)));
      if(names.size() > size) sb.append(", ...");
      sb.append(')');
    }
    System.out.println(sb);
  }

  /**
   * Prints the times as a Markdown table, with rows for the best time and the median.
   * @param types parsers
   * @param times times in milliseconds, by round and parser
   * @param warmup number of warm-up rounds
   */
  private static void table(final QueryParserType[] types, final long[][] times,
      final int warmup) {

    final int rounds = times.length;
    final String[][] rows = new String[rounds + 2][types.length + 1];
    for(int r = 0; r < rounds; r++) {
      rows[r][0] = r + 1 + (r < warmup ? " (warm-up)" : "");
      for(int t = 0; t < types.length; t++) rows[r][t + 1] = Long.toString(times[r][t]);
    }
    rows[rounds][0] = "best";
    rows[rounds + 1][0] = "median";
    for(int t = 0; t < types.length; t++) {
      final long[] measured = new long[rounds - warmup];
      for(int r = warmup; r < rounds; r++) measured[r - warmup] = times[r][t];
      rows[rounds][t + 1] = Long.toString(Arrays.stream(measured).min().getAsLong());
      rows[rounds + 1][t + 1] = Long.toString(median(measured));
    }
    final String[] heads = new String[types.length + 1];
    heads[0] = "Round";
    for(int t = 0; t < types.length; t++) heads[t + 1] = types[t] + " (ms)";
    System.out.print(MarkdownTable.format(heads, rows));
  }

  /**
   * Returns the median of values.
   * @param values values
   * @return median (the mean of the middle values if the number of values is even)
   */
  static long median(final long[] values) {
    final long[] sorted = values.clone();
    Arrays.sort(sorted);
    final int s = sorted.length;
    return s % 2 == 1 ? sorted[s / 2] : (sorted[s / 2 - 1] + sorted[s / 2]) / 2;
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

    final List<String> queries = new ArrayList<>(), skipped = new ArrayList<>(),
        missing = new ArrayList<>();
    final NodeList sets = db.parse(dir.resolve("catalog.xml").toFile()).
        getElementsByTagNameNS(NS, "test-set");
    for(int s = 0; s < sets.getLength(); s++) {
      final String name = ((Element) sets.item(s)).getAttribute("file");
      final Path file = dir.resolve(name);
      final Document set;
      try {
        set = db.parse(file.toFile());
      } catch(final IOException | org.xml.sax.SAXException ex) {
        skipped.add(name);
        continue;
      }
      final NodeList tests = set.getElementsByTagNameNS(NS, "test-case");
      for(int t = 0; t < tests.getLength(); t++) {
        final NodeList list = ((Element) tests.item(t)).getElementsByTagNameNS(NS, "test");
        if(list.getLength() == 0) continue;
        final Element test = (Element) list.item(0);
        if(test.hasAttribute("file")) {
          final Path query = file.resolveSibling(test.getAttribute("file"));
          if(Files.exists(query)) queries.add(Files.readString(query, StandardCharsets.UTF_8));
          else missing.add(dir.relativize(query).toString().replace('\\', '/'));
        } else {
          queries.add(test.getTextContent());
        }
      }
    }
    return new Corpus(queries, skipped, missing);
  }
}

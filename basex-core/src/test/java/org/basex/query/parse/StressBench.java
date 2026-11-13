package org.basex.query.parse;

import java.util.*;

import org.basex.core.*;
import org.basex.core.MainOptions.*;
import org.basex.query.*;

/**
 * Measures the time that the query parsers need for large inputs, which could take quadratic time.
 * The times are printed, not asserted. Progress is reported after each input, outside of the
 * timed region. A Markdown table shows the median and the best time of the measurement rounds
 * (those after the warm-up) for each parser.
 *
 * Arguments (optional): the number of rounds (default: 7) and of warm-up rounds (default: 2).
 * Both parsers are measured. Deeply nested input needs a deep stack: if a parser runs out of it,
 * the table shows {@code overflow}, which depends on the stack size of the JVM ({@code -Xss}).
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class StressBench {
  /** Private constructor. */
  private StressBench() { }

  /**
   * Main method.
   * @param args rounds, warm-up rounds (all optional)
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    final QueryParserType[] types = QueryParserType.values();
    final int rounds = args.length > 0 ? Integer.parseInt(args[0]) : 7;
    final int warmup = Math.min(rounds - 1, args.length > 1 ? Integer.parseInt(args[1]) : 2);

    final String[][] cases = {
      { "content run", "<a>" + "x".repeat(200_000) + "</a>" },
      { "attribute run", "<a b='" + "x".repeat(200_000) + "'/>" },
      { "alternating content", "<a>" + "x{1}".repeat(50_000) + "</a>" },
      { "arguments", "concat(" + "1,".repeat(19_999) + "1)" },
      { "nested arguments", "concat(".repeat(200) + "1" + ", 2)".repeat(200) },
      { "operands", "1" + "+1".repeat(50_000) },
      { "else if", "if(0) {0} else ".repeat(20_000) + "{1}" },
    };

    final String[] heads = new String[2 * types.length + 1];
    heads[0] = "Input";
    for(int t = 0; t < types.length; t++) {
      heads[2 * t + 1] = types[t] + " median (ms)";
      heads[2 * t + 2] = types[t] + " best (ms)";
    }
    final String[][] rows = new String[cases.length][heads.length];
    final Context context = new Context();
    try {
      for(int c = 0; c < cases.length; c++) {
        rows[c][0] = cases[c][0];
        for(int t = 0; t < types.length; t++) {
          final long[] times = new long[rounds];
          boolean overflow = false;
          for(int round = 0; round < rounds && !overflow; round++) {
            final long start = System.nanoTime();
            try(QueryContext qc = new QueryContext(context)) {
              qc.queryParser = types[t];
              qc.parse(cases[c][1], null);
            } catch(final QueryException ex) {
              // deeply nested input can exceed the stack, which depends on the parser
              if(ex.error() != QueryError.BASEX_OVERFLOW) {
                throw new IllegalStateException(cases[c][0] + ": " + ex.getMessage(), ex);
              }
              overflow = true;
            }
            times[round] = System.nanoTime() - start;
          }
          final long[] measured = Arrays.copyOfRange(times, warmup, rounds);
          rows[c][2 * t + 1] = overflow ? "overflow" : millis(ParseBench.median(measured));
          rows[c][2 * t + 2] = overflow ? "overflow" : millis(Arrays.stream(measured).min().
              getAsLong());
        }
        // progress, after the measurement
        System.out.println(cases[c][0] + ": done");
      }
    } finally {
      context.close();
    }
    System.out.println();
    System.out.print(MarkdownTable.format(heads, rows));
  }

  /**
   * Formats a time.
   * @param nanos nanoseconds
   * @return milliseconds
   */
  private static String millis(final long nanos) {
    return String.format(Locale.ENGLISH, "%.1f", nanos / 1e6);
  }
}

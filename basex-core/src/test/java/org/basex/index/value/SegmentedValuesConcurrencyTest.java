package org.basex.index.value;

import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Checks that the segmented value indexes stay consistent while a database is updated by several
 * clients: index-driven queries must return the same results as a full scan.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SegmentedValuesConcurrencyTest extends SandboxTest {
  /** Segment threshold of the tests. */
  private static final int THRESHOLD = 10;
  /** Default segment threshold. */
  private static final int DEFAULT = SegmentedValues.threshold;
  /** Words that are indexed. */
  private static final String[] WORDS = { "alpha", "beta", "gamma", "delta", "epsilon" };

  /** Lowers the segment threshold. */
  @BeforeAll public static void start() {
    SegmentedValues.threshold = THRESHOLD;
  }

  /** Restores the segment threshold. */
  @AfterAll public static void stop() {
    SegmentedValues.threshold = DEFAULT;
  }

  /** Creates a test database. */
  @BeforeEach public void init() {
    set(MainOptions.UPDINDEX, true);
    set(MainOptions.TOKENINDEX, true);
    execute(new CreateDB(NAME, "<root>" + "<b/>".repeat(THRESHOLD) + "</root>"));
    execute(new Close());
  }

  /** Drops the test database and restores the default options. */
  @AfterEach public void finish() {
    execute(new DropDB(NAME));
    set(MainOptions.UPDINDEX, true);
    set(MainOptions.TOKENINDEX, false);
    set(MainOptions.AUTOFLUSH, true);
  }

  /**
   * Runs the test with autoflush.
   * @throws Exception exception
   */
  @Test @Timeout(120) public void autoflush() throws Exception {
    run(20, 15);
  }

  /**
   * Runs the test without autoflush.
   * @throws Exception exception
   */
  @Test @Timeout(120) public void noAutoflush() throws Exception {
    set(MainOptions.AUTOFLUSH, false);
    run(20, 15);
  }

  /**
   * Runs the test: writers insert, replace and delete indexed entries and query them back.
   * @param clients number of clients
   * @param runs number of runs per client
   * @throws Exception exception
   */
  private static void run(final int clients, final int runs) throws Exception {
    final String db = _DB_GET.args(NAME);
    final ArrayList<Callable<?>> tasks = new ArrayList<>(clients);
    for(int c = 0; c < clients; c++) {
      final int client = c;
      tasks.add(() -> {
        for(int r = 0; r < runs; r++) {
          final String word = WORDS[(client + r) % WORDS.length];
          final String other = WORDS[(client + r + 1) % WORDS.length];
          switch(r % 3) {
            case 0 -> query("insert node <a t='" + word + " " + other + "'>" + word +
              "</a> into " + db + "/root");
            case 1 -> query("for $a in (" + db + "//a[. = '" + word + "'])[1] return (" +
              "replace value of node $a with '" + other + "', " +
              "replace value of node $a/@t with '" + other + "')");
            default -> query("delete node (" + db + "//a[. = '" + other + "'])[last()]");
          }
          lookup(word);
        }
        return null;
      });
    }
    parallel(tasks);

    final int[] counts = new int[WORDS.length];
    for(int w = 0; w < WORDS.length; w++) counts[w] = lookup(WORDS[w]);
    execute(new Close());
    execute(new Open(NAME));
    for(int w = 0; w < WORDS.length; w++) assertEquals(counts[w], lookup(WORDS[w]), WORDS[w]);
    execute(new Optimize());
    for(int w = 0; w < WORDS.length; w++) assertEquals(counts[w], lookup(WORDS[w]), WORDS[w]);
    execute(new Close());
    query(_DB_INSPECT.args(NAME) + "?issues ! (?check || ': ' || ?count)", "");
  }

  /**
   * Looks a word up via the text, attribute and token index and via a full scan, within a single
   * snapshot of the database.
   * @param word word to look up
   * @return number of hits
   */
  private static int lookup(final String word) {
    final String result = query(
      "let $db := " + _DB_GET.args(NAME) + "\n" +
      "let $counts := (\n" +
      "  count($db//a[matches(text(), '^" + word + "$')]),\n" +
      "  count($db//a[text() = '" + word + "']),\n" +
      "  count($db//a[matches(@t, '^" + word + "$')]),\n" +
      "  count($db//a[@t = '" + word + "']),\n" +
      "  count($db//a[matches(@t, '\\b" + word + "\\b')]),\n" +
      "  count($db//a[contains-token(@t, '" + word + "')])\n" +
      ")\n" +
      "return string-join($counts ! string(), '/')");
    final String[] counts = result.split("/");
    assertEquals(counts[0], counts[1], "text index differs for '" + word + "': " + result);
    assertEquals(counts[2], counts[3], "attribute index differs for '" + word + "': " + result);
    assertEquals(counts[4], counts[5], "token index differs for '" + word + "': " + result);
    return Integer.parseInt(counts[1]) + Integer.parseInt(counts[3]) + Integer.parseInt(counts[5]);
  }
}

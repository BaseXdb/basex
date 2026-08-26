package org.basex.index;

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
 * Checks that the value indexes stay consistent while a database is updated by several clients:
 * index-driven queries must return the same results as a full scan, and rebuilding the indexes
 * must not change any result.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class IndexUpdateConcurrencyTest extends SandboxTest {
  /** Words that are indexed. */
  private static final String[] WORDS = { "alpha", "beta", "gamma", "delta", "epsilon" };

  /**
   * Creates a test database with incremental index updates.
   */
  @BeforeEach public void init() {
    set(MainOptions.UPDINDEX, true);
    set(MainOptions.TEXTINDEX, true);
    set(MainOptions.ATTRINDEX, true);
    set(MainOptions.TOKENINDEX, true);
    set(MainOptions.AUTOFLUSH, false);
    execute(new CreateDB(NAME, "<root/>"));
  }

  /**
   * Drops the test database and restores the default options.
   */
  @AfterEach public void finish() {
    execute(new DropDB(NAME));
    set(MainOptions.UPDINDEX, false);
    set(MainOptions.TOKENINDEX, false);
    set(MainOptions.AUTOFLUSH, true);
  }

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test @Timeout(120) public void clients10runs20() throws Exception {
    run(10, 20);
  }

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test @Timeout(120) public void clients50runs10() throws Exception {
    run(50, 10);
  }

  /**
   * Runs the test: writers insert indexed entries and query them back.
   * @param clients number of clients
   * @param runs number of runs per client
   * @throws Exception exception
   */
  private static void run(final int clients, final int runs) throws Exception {
    final ArrayList<Callable<?>> tasks = new ArrayList<>(clients);
    for(int c = 0; c < clients; c++) {
      final int client = c;
      tasks.add(() -> {
        for(int r = 0; r < runs; r++) {
          final String word = WORDS[(client + r) % WORDS.length];
          query("insert node <a t='" + word + "'>" + word + "</a> into " +
              _DB_GET.args(NAME) + "/root");
          // index-driven lookups must agree with a full scan at any time
          lookup(word);
        }
        return null;
      });
    }
    parallel(tasks);

    // the incremental index must agree with a freshly built one
    final int[] counts = new int[WORDS.length];
    for(int w = 0; w < WORDS.length; w++) counts[w] = lookup(WORDS[w]);
    // a full optimization requires an unpinned database
    execute(new Close());
    query(_DB_OPTIMIZE.args(NAME, true));
    for(int w = 0; w < WORDS.length; w++) {
      assertEquals(counts[w], lookup(WORDS[w]), "Rebuilt index differs for '" + WORDS[w] + '\'');
    }

    // no entry may have been lost
    query("count(" + _DB_GET.args(NAME) + "//a)", clients * runs);
  }

  /**
   * Looks a word up via the text, attribute and token index and via a full scan. All four counts
   * are determined by a single query, and hence within a single snapshot of the database.
   * @param word word to look up
   * @return number of hits
   */
  private static int lookup(final String word) {
    final String result = query(
      "let $db := " + _DB_GET.args(NAME) + "\n" +
      "let $counts := (\n" +
      "  count($db//a[matches(text(), '^" + word + "$')]),\n" +
      "  count($db//a[text() = '" + word + "']),\n" +
      "  count($db//a[@t = '" + word + "']),\n" +
      "  count($db//a[contains-token(@t, '" + word + "')])\n" +
      ")\n" +
      "return if(count(distinct-values($counts)) = 1) then $counts[1] " +
      "else string-join($counts ! string(), '/')");
    // a single value means that scan, text, attribute and token index agree
    assertFalse(result.contains("/"),
        "scan/text/attribute/token counts differ for '" + word + "': " + result);
    return Integer.parseInt(result);
  }
}

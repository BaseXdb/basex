package org.basex.index.value;

import static org.basex.query.func.Function.*;

import java.util.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Applies random updates to a database with segmented value indexes and compares the index
 * entries and lookups with a scan after each step.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SegmentedValuesRandomTest extends SandboxTest {
  /** Segment threshold of the tests. */
  private static final int THRESHOLD = 8;
  /** Default segment threshold. */
  private static final int DEFAULT = SegmentedValues.threshold;
  /** Values. */
  private static final String[] VALUES = { "", "a", "b", "a b", "c", "ab", "b c a", "ä", "d" };
  /** Number of steps. */
  private static final int STEPS = 120;

  /** Lowers the segment threshold. */
  @BeforeAll public static void start() {
    SegmentedValues.threshold = THRESHOLD;
  }

  /** Restores the segment threshold. */
  @AfterAll public static void stop() {
    SegmentedValues.threshold = DEFAULT;
  }

  /** Finalizes a test. */
  @AfterEach public void after() {
    execute(new DropDB(NAME));
    set(MainOptions.UPDINDEX, true);
    set(MainOptions.TOKENINDEX, false);
    set(MainOptions.TEXTINCLUDE, "");
    set(MainOptions.ATTRINCLUDE, "");
    set(MainOptions.TOKENINCLUDE, "");
    set(MainOptions.AUTOFLUSH, true);
  }

  /**
   * Runs random updates.
   * @param seed random seed
   */
  @ParameterizedTest
  @ValueSource(ints = { 1, 2, 3, 4 })
  public void random(final int seed) {
    final Random rnd = new Random(seed);
    set(MainOptions.UPDINDEX, true);
    set(MainOptions.TOKENINDEX, true);
    // included names: renames change the inclusion of texts and attributes
    set(MainOptions.TEXTINCLUDE, "a");
    set(MainOptions.ATTRINCLUDE, "t");
    set(MainOptions.TOKENINCLUDE, "t");
    set(MainOptions.AUTOFLUSH, seed % 2 == 0);
    final StringBuilder sb = new StringBuilder("<x>");
    for(int i = 0; i < 20; i++) sb.append(element(rnd));
    execute(new CreateDB(NAME, sb.append("</x>").toString()));
    execute(new Close());

    final String db = _DB_GET.args(NAME);
    for(int step = 0; step < STEPS; step++) {
      final int count = Integer.parseInt(query("count(" + db + "/x/*)"));
      final int pos = count == 0 ? 1 : rnd.nextInt(count) + 1;
      final String node = "(" + db + "/x/*)[" + pos + "]";
      final String value = VALUES[rnd.nextInt(VALUES.length)];
      switch(count == 0 ? 0 : rnd.nextInt(9)) {
        case 0, 1 -> query("insert node " + element(rnd) + " into " + db + "/x");
        case 2 -> query("insert node (1 to " + (rnd.nextInt(12) + 1) + ") ! " + element(rnd) +
          " before " + node);
        case 3 -> query("delete node " + node);
        case 4 -> query("for $n in " + node + " return replace value of node $n with '" +
          value + "'");
        case 5 -> query("for $n in " + node + "/@* return replace value of node $n with '" +
          value + "'");
        case 6 -> query("for $n in " + node + " return rename node $n as '" +
          (rnd.nextBoolean() ? "a" : "b") + "'");
        case 7 -> query("for $n in " + node + "/@* return rename node $n as '" +
          (rnd.nextBoolean() ? "t" : "u") + "'");
        default -> {
          if(rnd.nextInt(3) == 0) {
            execute(new Open(NAME));
            execute(new Optimize());
          }
          execute(new Close());
        }
      }
      check(db);
    }
    query(_DB_INSPECT.args(NAME) + "?issues ! (?check || ': ' || ?count)", "");
  }

  /**
   * Returns a random element.
   * @param rnd random generator
   * @return element
   */
  private static String element(final Random rnd) {
    final String name = rnd.nextBoolean() ? "a" : "b", att = rnd.nextBoolean() ? "t" : "u";
    return "<" + name + " " + att + "='" + VALUES[rnd.nextInt(VALUES.length)] + "'>" +
      VALUES[rnd.nextInt(VALUES.length)] + "</" + name + ">";
  }

  /**
   * Compares the index entries and lookups with a scan.
   * @param db database access
   */
  private static void check(final String db) {
    query("string-join(" + _INDEX_TEXTS.args(NAME) + " ! (. || ':' || @count), ' ')",
      query("string-join(for $t in " + db + "//a/text() group by $v := string($t) " +
        "order by $v return $v || ':' || count($t), ' ')"));
    query("string-join(" + _INDEX_ATTRIBUTES.args(NAME) + " ! (. || ':' || @count), ' ')",
      query("string-join(for $t in " + db + "//@t group by $v := string($t) " +
        "order by $v return $v || ':' || count($t), ' ')"));
    query("string-join(" + _INDEX_TOKENS.args(NAME) + " ! (. || ':' || @count), ' ')",
      query("string-join(for $t in " + db + "//@t ! distinct-values(tokenize(.)) " +
        "group by $v := $t order by $v return $v || ':' || count($t), ' ')"));
    for(final String value : VALUES) {
      queryIndexScan("//a[text() = '" + value + "']");
      queryIndexScan("//*[@t = '" + value + "']");
      if(!value.isEmpty() && !value.contains(" ")) {
        queryIndexScan("//*[contains-token(@t, '" + value + "')]");
      }
    }
  }
}

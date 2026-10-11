package org.basex.query.index;

import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.query.expr.ft.*;
import org.basex.query.expr.index.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Tests index rewrites on databases with values that exceed the maximum indexed length.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class MaxLenIndexTest extends SandboxTest {
  /** Value that exceeds the maximum length. */
  private static final String LONG = "cafe".repeat(5);
  /** Database reference. */
  private static final String DB = _DB_GET.args(NAME);
  /** String range query. */
  private static final String RANGE = "count(" + DB + "//@v[. > 'b'][. < 'e'])";
  /** Wildcard query. */
  private static final String WILDCARDS =
      "count(" + DB + "//p[text() contains text 'abc.*' using wildcards])";

  /** Prepares a test. */
  @BeforeEach public void before() {
    set(MainOptions.MAXLEN, 10);
    set(MainOptions.FTINDEX, true);
  }

  /** Finalizes a test. */
  @AfterEach public void after() {
    execute(new DropDB(NAME));
    set(MainOptions.MAXLEN, MainOptions.MAXLEN.value());
    set(MainOptions.FTINDEX, MainOptions.FTINDEX.value());
  }

  /** String ranges. */
  @Test public void stringRange() {
    execute(new CreateDB(NAME, "<x><a v='d'/><a v='c'/></x>"));
    check(RANGE, 2, exists(StringRangeAccess.class));

    execute(new CreateDB(NAME, "<x><a v='d'/><a v='" + LONG + "'/></x>"));
    check(RANGE, 2, empty(StringRangeAccess.class));
    check("count(" + DB + "//a[text() > 'b'])", 0, empty(StringRangeAccess.class));
  }

  /** String ranges with bounds that exceed the maximum length. */
  @Test public void stringRangeLongBounds() {
    execute(new CreateDB(NAME, "<x><a v='b'/><a v='m'/><a v='t'/><a v='z'/></x>"));
    final String min = "a".repeat(12), max = "t".repeat(12);
    check("count(" + DB + "//@v[. > '" + min + "'][. < 's'])", 2, exists(StringRangeAccess.class));
    check("count(" + DB + "//@v[. >= '" + min + "'][. <= 't'])", 3,
        exists(StringRangeAccess.class));
    check("count(" + DB + "//@v[. > 'b'][. < '" + max + "'])", 2, exists(StringRangeAccess.class));
    check("count(" + DB + "//@v[. >= 'b'][. <= '" + max + "'])", 3,
        exists(StringRangeAccess.class));
  }

  /** Wildcard and fuzzy queries. */
  @Test public void fullText() {
    execute(new CreateDB(NAME, "<x><p>beta abcdefghijk</p><p>abcdefg</p></x>"));
    check(WILDCARDS, 2, empty(FTIndexAccess.class));
    check("count(" + DB + "//p[text() contains text 'abcdefghij' using fuzzy])", 1,
        empty(FTIndexAccess.class));
    check("count(" + DB + "//p[text() contains text 'beta'])", 1, exists(FTIndexAccess.class));

    execute(new CreateDB(NAME, "<x><p>beta abcdefghij</p><p>abcdefg</p></x>"));
    check(WILDCARDS, 2, exists(FTIndexAccess.class));
  }

  /** Long values added by updates, persistence, and reset by a rebuild. */
  @Test public void updates() {
    execute(new CreateDB(NAME, "<x><a v='d'/><a/><p>abcdefg</p></x>"));
    check(RANGE, 1, exists(StringRangeAccess.class));
    check(WILDCARDS, 1, exists(FTIndexAccess.class));

    query("insert node attribute v { '" + LONG + "' } into " + DB + "//a[2]");
    query("insert node <p>abcdefghijk</p> into " + DB + "/x");
    execute(new Close());
    check(RANGE, 2, empty(StringRangeAccess.class));
    check(WILDCARDS, 2, empty(FTIndexAccess.class));

    query("delete node (" + DB + "//a[2]/@v, " + DB + "//p[2])");
    query(_DB_OPTIMIZE.args(NAME, true));
    check(RANGE, 1, exists(StringRangeAccess.class));
    check(WILDCARDS, 1, exists(FTIndexAccess.class));
  }
}

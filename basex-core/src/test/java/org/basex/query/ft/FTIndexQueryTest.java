package org.basex.query.ft;

import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.query.expr.ft.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Tests that full-text queries yield the same result with and without a full-text index.
 * The queries against the shared document are already checked in both modes by {@link FTTest} and
 * {@link FTSeqTest}; this class covers extended features on dedicated documents.
 *
 * @author BaseX Team, BSD License
 * @author Dimitar Popov
 */
public final class FTIndexQueryTest extends SandboxTest {
  /** Drops the test databases. */
  @AfterEach public void tearDown() {
    execute(new DropDB(NAME));
    execute(new DropDB(NAME + "ix"));
    set(MainOptions.FTINDEX, false);
  }

  /**
   * Creates an unindexed and a full-text indexed database from the given input.
   * @param input input
   */
  private static void init(final String input) {
    execute(new CreateDB(NAME, input));
    set(MainOptions.FTINDEX, true);
    try {
      execute(new CreateDB(NAME + "ix", input));
    } finally {
      set(MainOptions.FTINDEX, false);
    }
  }

  /**
   * Asserts that a query returns the same result with and without a full-text index.
   * @param qu query
   */
  private static void assertQuery(final String qu) {
    execute(new Open(NAME));
    final String result = query(qu);
    execute(new Open(NAME + "ix"));
    assertEquals(result, query(qu), '\n' + qu + '\n');
  }

  /** Extended full-text features. */
  @Test public void ext() {
    init("<x>A x B</x>");
    assertQuery("//*[text() contains text 'A B' all words distance exactly 0 words]");
    assertQuery(_FT_MARK.args(" //*[text() contains text { 'A B' } all words], 'b'"));
    assertQuery(_FT_MARK.args(" //*[text() contains text 'A' ftand 'B'], 'b'"));
  }

  /** Counts must not include index candidates that are discarded by the following name test. */
  @Test public void count() {
    init("<xml><a>A</a><b>A</b></xml>");
    assertQuery(_FT_COUNT.args(" //a[text() contains text 'A']"));
    assertQuery(_FT_COUNT.args(" //a[. contains text 'A']"));
    assertQuery(_FT_COUNT.args(" //*[text() contains text 'A']"));
  }

  /** Full-text index with the string values of mixed-content elements. */
  @Test public void mixedIndex() {
    set(MainOptions.FTINDEX, true);
    set(MainOptions.FTMIXED, true);
    set(MainOptions.FTINCLUDE, "p");
    try {
      execute(new CreateDB(NAME, "<doc><p>a <b>b</b></p><b>c</b><p>d <p>e</p></p></doc>"));
      // elements are returned, and only included names are indexed
      query(_FT_SEARCH.args(NAME, "b") + " ! name()", "p");
      query(_FT_SEARCH.args(NAME, "c"), "");
      // nested elements are indexed on each level
      query(_FT_SEARCH.args(NAME, "e") + " ! string()", "d e\ne");
      // predicates on the indexed elements are rewritten for index access
      check("//p[. contains text 'e'] ! string()", "d e\ne", exists(FTIndexAccess.class));
      query(_FT_COUNT.args(" //p[. contains text 'e']"), 2);
      check("//p[. contains text 'a b'] ! string()", "a b", exists(FTIndexAccess.class));
      // other names, text nodes and unnamed steps are evaluated sequentially
      check("//b[. contains text 'c'] ! string()", "c", empty(FTIndexAccess.class));
      check("//p[text() contains text 'a'] ! string()", "a b", empty(FTIndexAccess.class));
      check("//*[. contains text 'e'] ! name()", "doc\np\np", empty(FTIndexAccess.class));

      // all elements are indexed: wildcard steps are rewritten as well
      set(MainOptions.FTINCLUDE, "*");
      execute(new CreateDB(NAME, "<doc><a><x>A</x></a><a><b><x>A</x></b></a></doc>"));
      // (the string value of the document element is a single token)
      check("//*[. contains text 'A'] ! name()", "a\nx\na\nb\nx", exists(FTIndexAccess.class));
      check("//a[* contains text 'A'] ! name()", "a\na", exists(FTIndexAccess.class));
      // other node tests are evaluated sequentially
      check("count(//node()[. contains text 'A'])", 7, empty(FTIndexAccess.class));
    } finally {
      set(MainOptions.FTINCLUDE, "");
      set(MainOptions.FTMIXED, false);
    }
  }

  /** Mixed content. */
  @Test public void mixedContent() {
    init("<mix>A<sub/>B</mix>");
    assertQuery("//mix[text()[1] contains text 'B']");

    init("<xml><mix>B<sub/>A</mix><mix>A<sub/>B</mix></xml>");
    assertQuery("//mix[text()[1] contains text 'B']");
    assertQuery("//mix[text() contains text 'A'][1]");
  }
}

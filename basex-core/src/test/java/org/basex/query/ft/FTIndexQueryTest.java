package org.basex.query.ft;

import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
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

  /** Mixed content. */
  @Test public void mixedContent() {
    init("<mix>A<sub/>B</mix>");
    assertQuery("//mix[text()[1] contains text 'B']");

    init("<xml><mix>B<sub/>A</mix><mix>A<sub/>B</mix></xml>");
    assertQuery("//mix[text()[1] contains text 'B']");
    assertQuery("//mix[text() contains text 'A'][1]");
  }
}

package org.basex.data;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.query.expr.index.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Tests for the statistics of a database.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StatisticsTest extends SandboxTest {
  /** Query for the documents of the database. */
  private static final String DOC = _DB_GET.args(NAME);

  /** Drops the database. */
  @AfterEach public void tearDown() {
    execute(new DropDB(NAME));
    set(MainOptions.MAINMEM, false);
  }

  /** Numeric comparisons take the string value of empty elements into account. */
  @Test public void emptyElementComparison() {
    execute(new CreateDB(NAME, "<x><b/><b>60</b></x>"));
    // the string value of the empty element cannot be cast to a number
    error(DOC + "//b[. > 50]", FUNCCAST_X_X);
    error(DOC + "//b[. = 7]", FUNCCAST_X_X);
    // exact string lookups are not affected
    query("count(" + DOC + "//b[. = '60'])", 1);
    query("count(" + DOC + "//b[. = ''])", 1);

    // without empty elements, the values are still looked up in the index
    execute(new CreateDB(NAME, "<x><b>10</b><b>60</b></x>"));
    check("count(" + DOC + "//b[. > 50])", 1, exists(ValueAccess.class));
  }

  /** Statistics of a main-memory database are finalized. */
  @Test public void mainmemStatistics() {
    set(MainOptions.MAINMEM, true);
    execute(new CreateDB(NAME, "<x><g/><g>8</g></x>"));
    // the string value of the empty element is not numeric
    error("min(" + DOC + "/x/g)", FUNCCAST_X_X);
    query("count(distinct-values(" + DOC + "/x/g))", 2);
  }
}

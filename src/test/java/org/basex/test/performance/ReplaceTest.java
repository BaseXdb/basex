package org.basex.test.performance;

import static org.junit.Assert.*;

import java.util.Random;

import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Set;
import org.basex.core.cmd.XQuery;
import org.basex.data.DataText;
import org.basex.util.Util;
import org.junit.Test;

/**
 * This class performs a local stress tests with a specified
 * number of threads and queries.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class ReplaceTest {
  /** Test database name. */
  private static final String DB = Util.name(ReplaceTest.class);
  /** Number of runs per client. */
  private static final int NQUERIES = 10000;
  /** Global context. */
  static final Context CONTEXT = new Context();
  /** Random number generator. */
  static final Random RND = new Random();
  /** Result counter. */
  static int counter;

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test
  public void run() throws Exception {
    new Set(Prop.TEXTINDEX, false).execute(CONTEXT);
    new Set(Prop.ATTRINDEX, false).execute(CONTEXT);

    // Create test database
    new CreateDB(DB, "<X><A>1</A><A>1</A></X>").execute(CONTEXT);

    final long len1 = CONTEXT.data().meta.dbfile(DataText.DATATXT).length();

    // deactivate flushing to speed up querying
    new Set(Prop.AUTOFLUSH, false).execute(CONTEXT);

    // replace texts with random doubles
    final Random rnd = new Random();
    for(int i = 0; i < NQUERIES; i++) {
      final double d = rnd.nextDouble();
      final String qu = "for $a in //A return replace node $a/text() with " + d;
      new XQuery(qu).execute(CONTEXT);
    }

    // perform final, flushed replacement
    new Set(Prop.AUTOFLUSH, true).execute(CONTEXT);

    final long len2 = CONTEXT.data().meta.dbfile(DataText.DATATXT).length();
    assertEquals(len1, len2);

    // Drop database
    new DropDB(DB).execute(CONTEXT);
    CONTEXT.close();
  }
}

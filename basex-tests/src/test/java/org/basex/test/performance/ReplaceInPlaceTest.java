package org.basex.test.performance;

import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.test.*;
import org.junit.*;

/**
 * This test replaces texts in-place and checks if the database text files increase
 * in size. Currently, the actual test is wrapped in comments.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class ReplaceInPlaceTest extends SandboxTest {
  /** Number of queries to be run. */
  private static final int NQUERIES = 10000;
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
    context.options.set(Options.TEXTINDEX, false);
    context.options.set(Options.ATTRINDEX, false);
    context.options.set(Options.AUTOFLUSH, false);

    // create test database
    new CreateDB(NAME, "<X>" +
        "<A>x.xxxxxxxxxxxxxxxxxx</A>" +
        "<A>x.xxxxxxxxxxxxxxxxxx</A></X>").execute(context);

    //final long len1 = CONTEXT.data().meta.dbfile(DataText.DATATXT).length();

    // replace texts with random doubles
    final Random rnd = new Random();
    for(int i = 0; i < NQUERIES; i++) {
      final double d = rnd.nextDouble();
      final String qu = "for $a in //A return replace node $a/text() with " + d;
      new XQuery(qu).execute(context);
    }

    // perform final, flushed replacement
    new Flush().execute(context);

    //final long len2 = CONTEXT.data().meta.dbfile(DataText.DATATXT).length();
    //assertEquals(len1, len2);

    // Drop database
    new DropDB(NAME).execute(context);
  }
}

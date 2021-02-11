package org.basex.local.single;

import java.util.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.Test;

/**
 * This test replaces texts in-place and checks if the database text files increase
 * in size. Currently, the actual test is wrapped in comments.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ReplaceInPlaceTest extends SandboxTest {
  /** Number of queries to be run. */
  private static final int NQUERIES = 10000;
  /** Random number generator. */
  static final Random RND = new Random();
  /** Result counter. */
  static int counter;

  /** Runs the test. */
  @Test public void run() {
    set(MainOptions.TEXTINDEX, false);
    set(MainOptions.ATTRINDEX, false);
    set(MainOptions.AUTOFLUSH, false);

    // create test database
    execute(new CreateDB(NAME, "<X>" +
        "<A>x.xxxxxxxxxxxxxxxxxx</A>" +
        "<A>x.xxxxxxxxxxxxxxxxxx</A></X>"));

    // replace texts with random doubles
    final Random rnd = new Random();
    for(int i = 0; i < NQUERIES; i++) {
      query("for $a in //A return replace node $a/text() with " + rnd.nextDouble());
    }

    // perform final, flushed replacement
    execute(new Flush());

    // Drop database
    execute(new DropDB(NAME));
  }
}

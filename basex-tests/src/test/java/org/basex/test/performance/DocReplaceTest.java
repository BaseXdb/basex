package org.basex.test.performance;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.test.*;
import org.junit.*;

/**
 * This class replaces document nodes in a database.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class DocReplaceTest extends SandboxTest {
  /** Number of queries to be run. */
  private static final int NQUERIES = 25000;

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test
  public void run() throws Exception {
    context.options.set(MainOptions.TEXTINDEX, false);
    context.options.set(MainOptions.ATTRINDEX, false);
    context.options.set(MainOptions.AUTOFLUSH, false);

    // create test database
    new CreateDB(NAME).execute(context);

    // replace nodes
    for(int i = 0; i < NQUERIES; i++) {
      new Add(i + IO.XMLSUFFIX, "<a/>").execute(context);
    }

    // replace nodes with same content
    for(int i = 0; i < NQUERIES; i++) {
      new Replace(i + IO.XMLSUFFIX, "<a/>").execute(context);
    }

    // Drop database
    new DropDB(NAME).execute(context);
  }
}

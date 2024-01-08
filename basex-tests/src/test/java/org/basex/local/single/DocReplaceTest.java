package org.basex.local.single;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.junit.jupiter.api.Test;

/**
 * This class replaces document nodes in a database.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class DocReplaceTest extends SandboxTest {
  /** Number of queries to be run. */
  private static final int NQUERIES = 25000;

  /** Runs the test. */
  @Test public void run() {
    set(MainOptions.TEXTINDEX, false);
    set(MainOptions.ATTRINDEX, false);
    set(MainOptions.AUTOFLUSH, false);
    set(MainOptions.INTPARSE, true);

    // create test database
    execute(new CreateDB(NAME));

    // add documents
    for(int n = 0; n < NQUERIES; n++) execute(new Add(n + IO.XMLSUFFIX, "<a/>"));
    execute(new Flush());

    // replace documents with same content
    for(int n = 0; n < NQUERIES; n++) execute(new Put(n + IO.XMLSUFFIX, "<a/>"));

    // Drop database
    execute(new DropDB(NAME));
  }
}

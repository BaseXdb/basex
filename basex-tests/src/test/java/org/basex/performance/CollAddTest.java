package org.basex.performance;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.junit.*;
import org.junit.Test;

/**
 * This class adds and retrieves documents in a collection.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class CollAddTest extends SandboxTest {
  /**
   * Initializes the tests.
   * @throws Exception exception
   */
  @BeforeClass
  public static void init() throws Exception {
    new Set(MainOptions.INTPARSE, true).execute(context);
  }

  /**
   * Adds 100 documents.
   * @throws Exception exception
   */
  @Test
  public void add100() throws Exception {
    add(100, false);
  }

  /**
   * Adds 1000 documents.
   * @throws Exception exception
   */
  @Test
  public void add1000() throws Exception {
    add(1000, false);
  }

  /**
   * Adds 10000 documents.
   * @throws Exception exception
   */
  @Test
  public void add10000() throws Exception {
    add(10000, false);
  }

  /**
   * Adds 100000 documents.
   * @throws Exception exception
   */
  @Test
  public void add100000() throws Exception {
    add(100000, false);
  }

  /**
   * Adds 1000 documents.
   * @throws Exception exception
   */
  @Test
  public void add100Force() throws Exception {
    add(100, true);
  }

  /**
   * Adds 1000 documents.
   * @throws Exception exception
   */
  @Test
  public void add1000Force() throws Exception {
    add(1000, true);
  }

  /**
   * Adds 1000 documents.
   * @throws Exception exception
   */
  @Test
  public void add10000Force() throws Exception {
    add(10000, true);
  }

  /**
   * Creates a database.
   * @param n number of documents to be added
   * @param flush force flush of updates
   * @throws Exception exception
   */
  private static void add(final int n, final boolean flush) throws Exception {
    new Set(MainOptions.AUTOFLUSH, flush).execute(context);

    // Create test database
    final Command cmd = new CreateDB(NAME);
    cmd.execute(context);
    // Add documents
    for(int i = 0; i < n; i++) {
      new Add(Integer.toString(i), "<xml/>").execute(context);
    }
    // Close database
    new DropDB(NAME).execute(context);
  }
}

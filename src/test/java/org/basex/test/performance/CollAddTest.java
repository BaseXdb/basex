package org.basex.test.performance;

import org.basex.core.Command;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Set;
import org.basex.util.Util;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class adds and retrieves documents in a collection.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class CollAddTest {
  /** Test database name. */
  private static final String DB = Util.name(CollAddTest.class);
  /** Global context. */
  private static final Context CONTEXT = new Context();

  /**
   * Initializes the tests.
   * @throws Exception exception
   */
  @BeforeClass
  public static void init() throws Exception {
    new Set(Prop.INTPARSE, true).execute(CONTEXT);
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
    new Set(Prop.AUTOFLUSH, flush).execute(CONTEXT);

    // Create test database
    final Command cmd = new CreateDB(DB);
    cmd.execute(CONTEXT);
    // Add documents
    for(int i = 0; i < n; i++) {
      new Add(Integer.toString(i), "<xml/>").execute(CONTEXT);
    }
    // Close database
    new DropDB(DB).execute(CONTEXT);
  }
}

package org.basex.performance;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.junit.*;
import org.junit.Test;

/**
 * This class adds and retrieves documents in a collection.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class CollStressTest extends SandboxTest {
  /** Number of documents to be added. */
  private static final int SIZE = 4000;

  /**
   * Initializes the tests.
   * @throws Exception exception
   */
  @BeforeClass
  public static void init() throws Exception {
    final CreateDB cmd = new CreateDB(NAME);
    cmd.execute(context);
    // Speed up updates and add documents
    new Set(MainOptions.AUTOFLUSH, false).execute(context);
    new Set(MainOptions.INTPARSE, true).execute(context);
    for(int i = 0; i < SIZE; i++) {
      new Add(Integer.toString(i), "<xml/>").execute(context);
    }
    new Set(MainOptions.AUTOFLUSH, true).execute(context);
  }

  /**
   * Finishes the tests.
   * @throws Exception exception
   */
  @AfterClass
  public static void finish() throws Exception {
    new DropDB(NAME).execute(context);
  }

  /**
   * Requests specific documents.
   * @throws Exception exception
   */
  @Test
  public void specificOpened() throws Exception {
    new Open(NAME).execute(context);
    for(int i = 0; i < SIZE; i++) {
      new XQuery("collection('" + NAME + '/' + i + "')").execute(context);
    }
  }

  /**
   * Requests specific documents from closed database.
   * @throws Exception exception
   */
  @Test
  public void specificClosed() throws Exception {
    new Close().execute(context);
    for(int i = 0; i < SIZE; i++) {
      new XQuery("collection('" + NAME + '/' + i + "')").execute(context);
    }
  }

  /**
   * Requests all documents.
   * @throws Exception exception
   */
  @Test
  public void allOpened() throws Exception {
    new Open(NAME).execute(context);
    new XQuery("for $i in 0 to " + (SIZE - 1) + ' ' +
      "return collection(concat('" + NAME + "/', $i))").execute(context);
  }

  /**
   * Requests all documents from closed database.
   * @throws Exception exception
   */
  @Test
  public void allClosed() throws Exception {
    new Close().execute(context);
    new XQuery("for $i in 0 to " + (SIZE - 1) + ' ' +
      "return collection(concat('" + NAME + "/', $i))").execute(context);
  }
}

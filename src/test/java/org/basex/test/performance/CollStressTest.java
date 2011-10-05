package org.basex.test.performance;

import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.Set;
import org.basex.core.cmd.XQuery;
import org.basex.util.Util;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class adds and retrieves documents in a collection.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class CollStressTest {
  /** Test database name. */
  private static final String DB = Util.name(CollStressTest.class);
  /** Global context. */
  private static final Context CONTEXT = new Context();
  /** Number of documents to be added. */
  private static final int SIZE = 4000;

  /**
   * Initializes the tests.
   * @throws Exception exception
   */
  @BeforeClass
  public static void init() throws Exception {
    final CreateDB cmd = new CreateDB(DB);
    cmd.execute(CONTEXT);
    // Speed up updates and add documents
    new Set(Prop.AUTOFLUSH, false).execute(CONTEXT);
    new Set(Prop.INTPARSE, true).execute(CONTEXT);
    for(int i = 0; i < SIZE; i++) {
      new Add(Integer.toString(i), "<xml/>").execute(CONTEXT);
    }
    new Set(Prop.AUTOFLUSH, true).execute(CONTEXT);
  }

  /**
   * Finishes the tests.
   * @throws Exception exception
   */
  @AfterClass
  public static void finish() throws Exception {
    new DropDB(DB).execute(CONTEXT);
  }

  /**
   * Requests specific documents.
   * @throws Exception exception
   */
  @Test
  public void specificOpened() throws Exception {
    new Open(DB).execute(CONTEXT);
    for(int i = 0; i < SIZE; i++) {
      new XQuery("collection('" + DB + "/" + i + "')").execute(CONTEXT);
    }
  }

  /**
   * Requests specific documents from closed database.
   * @throws Exception exception
   */
  @Test
  public void specificClosed() throws Exception {
    new Close().execute(CONTEXT);
    for(int i = 0; i < SIZE; i++) {
      new XQuery("collection('" + DB + "/" + i + "')").execute(CONTEXT);
    }
  }

  /**
   * Requests all documents.
   * @throws Exception exception
   */
  @Test
  public void allOpened() throws Exception {
    new Open(DB).execute(CONTEXT);
    new XQuery("for $i in 0 to " + (SIZE - 1) + " " +
      "return collection(concat('" + DB + "/', $i))").execute(CONTEXT);
  }

  /**
   * Requests all documents from closed database.
   * @throws Exception exception
   */
  @Test
  public void allClosed() throws Exception {
    new Close().execute(CONTEXT);
    new XQuery("for $i in 0 to " + (SIZE - 1) + " " +
      "return collection(concat('" + DB + "/', $i))").execute(CONTEXT);
  }
}

package org.basex.examples.query;

import org.basex.core.Context;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.XQuery;

/**
 * This class adds and retrieves documents in a collection.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public final class CollStressTest {
  /** Global context. */
  private static final Context CONTEXT = new Context();
  /** Number of documents to be added. */
  private static final int SIZE = 1000;

  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    System.out.println("=== CollectionTest ===");

    // Create test database
    System.out.println("\n* Create test database.");
    final CreateDB cmd = new CreateDB("test");
    cmd.execute(CONTEXT);
    System.out.print(cmd.info());

    // Add documents
    for(int i = 0; i < SIZE; i++) {
      new Add("<xml/>", Integer.toString(i)).execute(CONTEXT);
    }
    System.out.print("\n* " + SIZE + " documents added.");

    // Find documents
    for(int i = 0; i < SIZE; i++) {
      new XQuery("collection('test/" + i + "')").execute(CONTEXT);
    }
    System.out.print("\n* " + SIZE + " documents queries.");
  }
}

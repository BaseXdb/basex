package org.basex.test.performance;

import org.basex.core.Context;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.XQuery;
import org.basex.util.Performance;

/**
 * This class adds and retrieves documents in a collection.
 *
 * @author BaseX Team 2005-11, BSD License
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

    Performance perf = new Performance();

    // Add documents
    for(int i = 0; i < SIZE; i++) {
      new Add("<xml/>", Integer.toString(i)).execute(CONTEXT);
    }
    System.out.println("\n* " + SIZE + " documents added: " + perf);

    // Request specific documents
    for(int i = 0; i < SIZE; i++) {
      new XQuery("collection('test/" + i + "')").execute(CONTEXT);
    }
    System.out.println("\n* Request specific documents: " + perf);

    // Close database
    new Close().execute(CONTEXT);

    // Request specific documents (open database by XQuery processor)
    for(int i = 0; i < SIZE; i++) {
      new XQuery("collection('test/" + i + "')").execute(CONTEXT);
    }
    System.out.println("\n* Request specific documents (db closed): " + perf);

    new Open("test").execute(CONTEXT);

    // Loop through all documents
    new XQuery("for $i in 0 to " + (SIZE - 1) + " " +
      "return collection(concat('test/', $i))").execute(CONTEXT);
    System.out.println("\n* Loop through documents: " + perf);

    // Close database
    new Close().execute(CONTEXT);

    // Loop through all documents (open database by XQuery processor)
    new XQuery("for $i in 0 to " + (SIZE - 1) + " " +
      "return collection(concat('test/', $i))").execute(CONTEXT);
    System.out.println("\n* Loop through documents (db closed): " + perf);

    new DropDB("test").execute(CONTEXT);
  }
}

package org.basex.test.performance;

import org.basex.core.Command;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.Set;
import org.basex.util.Performance;

/**
 * This class adds and retrieves documents in a collection.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class CollAddTest {
  /** Global context. */
  private static final Context CONTEXT = new Context();
  /** Number of documents to be added. */
  private static final int SIZE = 8000;

  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    System.out.println("=== CollAddTest ===");

    new Set(Prop.INTPARSE, true).execute(CONTEXT);

    // Create test database
    System.out.println("\n* Create test database.");
    Command cmd = new CreateDB("test");
    cmd.execute(CONTEXT);
    System.out.print(cmd.info());

    final Performance perf = new Performance();

    // Add documents
    for(int i = 0; i < SIZE; i++) {
      new Add("<xml/>", Integer.toString(i)).execute(CONTEXT);
    }
    System.out.println("\n* " + SIZE + " documents added: " + perf);

    // Create test database
    System.out.println("\n* Create test database.");
    cmd = new CreateDB("test");
    cmd.execute(CONTEXT);
    System.out.print(cmd.info());

    perf.getTimer();

    // Add documents
    for(int i = 0; i < SIZE; i++) {
      new Add("<xml/>", Integer.toString(i)).execute(CONTEXT);
    }
    System.out.println("\n* " + SIZE + " documents added: " + perf);
  }
}

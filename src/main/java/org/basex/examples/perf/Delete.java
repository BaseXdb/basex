package org.basex.examples.perf;

/**
 * This class benchmarks delete operations.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public final class Delete extends Benchmark {
  /**
   * Runs the example code.
   * @param args command-line arguments
   * @throws Exception exception
   */
  public static void main(final String... args) throws Exception {
    new Delete(args);
  }

  /**
   * Private constructor.
   * @param args command-line arguments
   * @throws Exception exception
   */
  private Delete(final String... args) throws Exception {
    if(!init(args)) return;

    update("delete node //node()");

    // drop database
    finish();
  }
}

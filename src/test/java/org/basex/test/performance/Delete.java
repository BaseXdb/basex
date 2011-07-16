package org.basex.test.performance;

/**
 * This class benchmarks delete operations.
 *
 * @author BaseX Team 2005-11, BSD License
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

    // delete root node
    update("delete node /*");

    // delete all nodes
    update("delete node //node()");

    // delete all elements
    update("delete node //*");

    // delete all text nodes
    update("delete node //text()");

    // delete all text nodes in several runs
    final String qu = query("count(//text())");
    final int n = Math.min(1000, Integer.parseInt(qu.trim()));
    update(n, "delete node (//text())[1]");

    // delete all text nodes
    update("for $i in 1 to count(//text()) " +
        "return delete node /descendant::text()[$i]");

    finish();
  }
}

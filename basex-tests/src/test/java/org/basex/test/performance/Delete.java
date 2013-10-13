package org.basex.test.performance;

import org.junit.*;

/**
 * This class benchmarks delete operations.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class Delete extends Benchmark {
  /**
   * Deletes the root node.
   * @throws Exception exception
   */
  @Test
  public void root() throws Exception {
    update("delete node /*");
  }

  /**
   * Deletes all nodes.
   * @throws Exception exception
   */
  @Test
  public void nodes() throws Exception {
    update("delete node //node()");
  }

  /**
   * Deletes all element nodes.
   * @throws Exception exception
   */
  @Test
  public void elements() throws Exception {
    update("delete node //*");
  }

  /**
   * Deletes all text nodes.
   * @throws Exception exception
   */
  @Test
  public void texts() throws Exception {
    update("delete node //text()");
  }

  /**
   * Deletes first 1000 text nodes.
   * @throws Exception exception
   */
  @Test
  public void texts1000() throws Exception {
    final String qu = query("count(//text())");
    final int n = Math.min(1000, Integer.parseInt(qu.trim()));
    update(n, "delete node (//text())[1]");
  }

  /**
   * Deletes first 1000 text nodes one by one.
   * @throws Exception exception
   */
  @Test
  public void textsSingle1000() throws Exception {
    update("for $i in 1 to min((1000, count(//text()))) " +
        "return delete node /descendant::text()[$i]");
  }
}

package org.basex.local.single;

import org.junit.jupiter.api.*;

/**
 * This class benchmarks delete operations.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Delete extends Benchmark {
  /**
   * Deletes the root node.
   * @throws Exception exception
   */
  @Test public void root() throws Exception {
    eval("delete node /*");
  }

  /**
   * Deletes all nodes.
   * @throws Exception exception
   */
  @Test public void nodes() throws Exception {
    eval("delete node //node()");
  }

  /**
   * Deletes all element nodes.
   * @throws Exception exception
   */
  @Test public void elements() throws Exception {
    eval("delete node //*");
  }

  /**
   * Deletes all text nodes.
   * @throws Exception exception
   */
  @Test public void texts() throws Exception {
    eval("delete node //text()");
  }

  /**
   * Deletes first 1000 text nodes.
   * @throws Exception exception
   */
  @Test public void texts1000() throws Exception {
    final String qu = eval("count(//text())");
    final int n = Math.min(1000, Integer.parseInt(qu.trim()));
    eval(n, "delete node (//text())[1]");
  }

  /**
   * Deletes first 1000 text nodes one by one.
   * @throws Exception exception
   */
  @Test public void textsSingle1000() throws Exception {
    eval("for $i in 1 to min((1000, count(//text()))) " +
        "return delete node /descendant::text()[$i]");
  }
}

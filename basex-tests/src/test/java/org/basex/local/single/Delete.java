package org.basex.local.single;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/**
 * This class benchmarks delete operations.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Delete extends Benchmark {
  /**
   * Deletes the root node.
   * @throws Exception exception
   */
  @Test public void root() throws Exception {
    eval("delete node /*");
    assertEquals(0, count("/*"));
  }

  /**
   * Deletes all nodes.
   * @throws Exception exception
   */
  @Test public void nodes() throws Exception {
    eval("delete node //node()");
    assertEquals(0, count("//node()"));
  }

  /**
   * Deletes all element nodes.
   * @throws Exception exception
   */
  @Test public void elements() throws Exception {
    eval("delete node //*");
    assertEquals(0, count("//*"));
  }

  /**
   * Deletes all text nodes.
   * @throws Exception exception
   */
  @Test public void texts() throws Exception {
    eval("delete node //text()");
    assertEquals(0, count("//text()"));
  }

  /**
   * Deletes first 1000 text nodes.
   * @throws Exception exception
   */
  @Test public void texts1000() throws Exception {
    final int texts = count("//text()");
    final int n = Math.min(1000, texts);
    eval(n, "delete node (//text())[1]");
    assertEquals(texts - n, count("//text()"));
  }

  /**
   * Deletes first 1000 text nodes one by one.
   * @throws Exception exception
   */
  @Test public void textsSingle1000() throws Exception {
    final int texts = count("//text()");
    final int n = Math.min(1000, texts);
    eval("for $i in 1 to " + n + " return delete node /descendant::text()[$i]");
    assertEquals(texts - n, count("//text()"));
  }

  /**
   * Counts the nodes that are addressed by the specified path.
   * @param path path expression
   * @return number of nodes
   * @throws Exception exception
   */
  private static int count(final String path) throws Exception {
    return Integer.parseInt(eval("count(" + path + ')').trim());
  }
}

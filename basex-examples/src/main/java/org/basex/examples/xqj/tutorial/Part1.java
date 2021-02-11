package org.basex.examples.xqj.tutorial;

import javax.xml.xquery.*;

import net.xqj.basex.*;

/**
 * XQJ Examples, derived from an
 * <a href="https://www.progress.com/products/data-integration-suite/data-integration-suite-developer-center/data-integration-suite-tutorials/learning-xquery/introduction-to-the-xquery-api-for-java-xqj-">
 * XQJ online tutorial</a>.
 *
 * Part 1: An XQJ Introduction.
 *
 * @author BaseX Team 2005-21, BSD License
 */
public final class Part1 extends Main {
  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String... args) throws Exception {
    init("1: An XQJ Introduction");

    // Print 'Hello World!'
    info("Print 'Hello World'");

    XQDataSource xqjd = new BaseXXQDataSource();
    XQConnection xqjc = xqjd.getConnection("admin", "admin");
    XQExpression xqje = xqjc.createExpression();
    XQSequence xqjs = xqje.executeQuery("'Hello World!'");
    xqjs.writeSequence(System.out, null);
    xqjc.close();

    // Flush output.
    System.out.println();
  }
}

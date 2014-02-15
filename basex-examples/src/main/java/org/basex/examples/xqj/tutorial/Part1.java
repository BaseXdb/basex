package org.basex.examples.xqj.tutorial;

import javax.xml.xquery.*;

import net.xqj.basex.*;

/**
 * XQJ Example, derived from the XQJ Tutorial
 * <a href="http://www.xquery.com/tutorials/xqj_tutorial">
 * http://www.xquery.com/tutorials/xqj_tutorial</a>
 * from Marc van Cappellen.
 *
 * Part 1: An XQJ Introduction.
 *
 * @author BaseX Team 2005-14, BSD License
 */
public final class Part1 extends Main {
  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
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

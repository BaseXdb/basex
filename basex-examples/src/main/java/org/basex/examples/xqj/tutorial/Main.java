package org.basex.examples.xqj.tutorial;

import javax.xml.xquery.*;

import net.xqj.basex.*;

/**
 * XQJ Examples, derived from an
 * <a href="https://www.progress.com/products/data-integration-suite/data-integration-suite-developer-center/data-integration-suite-tutorials/learning-xquery/introduction-to-the-xquery-api-for-java-xqj-">
 * XQJ online tutorial</a>.
 *
 * @author BaseX Team 2005-21, BSD License
 */
abstract class Main {
  /**
   * Initializes the query example.
   * @param info info
   */
  static void init(final String info) {
    System.out.println("=== XQJ Tutorial/xquery.com ===");
    System.out.println("Part " + info);
  }

  /**
   * Creates and returns a default connection.
   * @return connection instance
   */
  static XQConnection connect() {
    return new BaseXXQDataSource().getConnection("admin", "admin");
  }

  /**
   * Closes the specified connection.
   * @param xqc connection to be closed
   * @throws XQException connection exception
   */
  static void close(final XQConnection xqc) throws XQException {
    xqc.close();
  }

  /**
   * Shows some query info.
   * @param info info text
   */
  static void info(final String info) {
    System.out.println("\n* " + info);
  }

  /**
   * Prints the entries of a sequence.
   * @param info query information
   * @param xqs sequence
   * @throws XQException query exception
   */
  static void print(final String info, final XQSequence xqs) throws XQException {
    info(info);
    xqs.writeSequence(System.out, null);
    System.out.println();
  }

  /**
   * Prints the result of an expression.
   * @param info query info
   * @param xqp prepared query expression
   */
  static void print(final String info, final XQPreparedExpression xqp) {
    info(info);
    try {
      xqp.executeQuery().writeSequence(System.out, null);
    } catch(final XQException ex) {
      ex.printStackTrace();
    }
    System.out.println();
  }
}

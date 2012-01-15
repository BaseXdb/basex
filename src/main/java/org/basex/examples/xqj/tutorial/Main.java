package org.basex.examples.xqj.tutorial;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQSequence;
import org.basex.api.xqj.BXQDataSource;

/**
 * XQJ Examples, derived from the XQJ Tutorial
 * <a href="http://www.xquery.com/tutorials/xqj_tutorial">
 * http://www.xquery.com/tutorials/xqj_tutorial</a>
 * from Marc van Cappellen.
 *
 * @author BaseX Team 2005-12, BSD License
 */
abstract class Main {
  /**
   * Initializes the query example.
   * @param info info
   */
  static void init(final String info) {
    System.out.println("=== XQJ Tutorial/xquery.com ===");
    System.out.println("Part " + info + "");
  }

  /**
   * Creates and returns a default connection.
   * @return connection instance
   * @throws XQException connection exception
   */
  static XQConnection connect() throws XQException {
    return new BXQDataSource().getConnection();
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
  static void print(final String info, final XQSequence xqs)
      throws XQException {

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
      System.out.println(ex);
    }
    System.out.println();
  }
}

package org.basex.examples.xqj.cfoster;

import javax.xml.xquery.*;

import net.xqj.basex.*;

/**
 * XQJ Examples, derived from the XQJ Tutorial
 * <a href="http://www.cfoster.net/articles/xqj-tutorial">
 * http://www.cfoster.net/articles/xqj-tutorial</a> from Charles Foster.
 *
 * @author BaseX Team 2005-21, BSD License
 */
abstract class Main {
  /**
   * Initializes the query example.
   * @param info info
   */
  static void init(final String info) {
    System.out.println("=== XQJ Tutorial/cfoster.net ===");
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
}

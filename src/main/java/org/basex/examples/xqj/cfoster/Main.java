package org.basex.examples.xqj.cfoster;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import org.basex.api.xqj.BXQDataSource;

/**
 * XQJ Examples, derived from the XQJ Tutorial
 * <a href="http://www.cfoster.net/articles/xqj-tutorial">
 * http://www.cfoster.net/articles/xqj-tutorial</a> from Charles Foster.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public abstract class Main {
  /** Private constructor. */
  protected Main() { }

  /**
   * Initializes the query example.
   * @param info info
   */
  static void init(final String info) {
    System.out.println("=== XQJ Tutorial/cfoster.net ===");
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
}

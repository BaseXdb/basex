package org.basex.examples.xqj.cfoster;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;

import net.xqj.basex.*;

/**
 * XQJ Example, derived from the XQJ Tutorial
 * <a href="http://www.cfoster.net/articles/xqj-tutorial">
 * http://www.cfoster.net/articles/xqj-tutorial</a> from Charles Foster.
 *
 * Part 1: Setting up your environment.
 *
 * @author BaseX Team 2005-12, BSD License
 */
public final class Part1 extends Main {
  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    init("1: Setting up your environment");

    // Connect and disconnect
    info("Connect and disconnect");

    // Default User name and Password
    XQDataSource xqs = new BaseXXQDataSource();
    XQConnection conn = xqs.getConnection("admin", "admin");
    info("Connected.");

    // Database XQueries and Updates performed here (covered later!)

    // Closing connection to the Database.
    conn.close();
    info("Disonnected.");
  }
}

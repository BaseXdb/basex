package org.basex.examples.xqj.tutorial;

import java.util.Properties;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;

/**
 * XQJ Example, derived from the XQJ Tutorial
 * <a href="http://www.xquery.com/tutorials/xqj_tutorial">
 * http://www.xquery.com/tutorials/xqj_tutorial</a>
 * from Marc van Cappellen.
 *
 * Part 2: Configuring XQJ Connections.
 *
 * @author BaseX Team 2005-12, BSD License
 */
public final class Part2 extends Main {
  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    init("2: Configuring XQJ Connections");

    // Set data source via property
    info("Set data source via property");
    Properties p = new Properties();
    p.setProperty("ClassName", "org.basex.api.xqj.BXQDataSource");

    String xqdsClassName = p.getProperty("ClassName");
    Class<?> xqdsClass = Class.forName(xqdsClassName);
    XQDataSource xqds = (XQDataSource) xqdsClass.newInstance();

    // Connect with user name and password
    info("Connect with user name and password");
    XQConnection xqjc = xqds.getConnection("admin", "admin");

    // Close the connection
    info("Close successful connection");
    xqjc.close();
  }
}

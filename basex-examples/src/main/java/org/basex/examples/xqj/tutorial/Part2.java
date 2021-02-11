package org.basex.examples.xqj.tutorial;

import java.util.*;

import javax.xml.xquery.*;

/**
 * XQJ Examples, derived from an
 * <a href="https://www.progress.com/products/data-integration-suite/data-integration-suite-developer-center/data-integration-suite-tutorials/learning-xquery/introduction-to-the-xquery-api-for-java-xqj-">
 * XQJ online tutorial</a>.
 *
 * Part 2: Configuring XQJ Connections.
 *
 * @author BaseX Team 2005-21, BSD License
 */
public final class Part2 extends Main {
  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String... args) throws Exception {
    init("2: Configuring XQJ Connections");

    // Set data source via property
    info("Set data source via property");
    Properties p = new Properties();
    p.setProperty("ClassName", "net.xqj.basex.BaseXXQDataSource");

    String xqdsClassName = p.getProperty("ClassName");
    Class<?> xqdsClass = Class.forName(xqdsClassName);
    XQDataSource xqds = (XQDataSource) xqdsClass.getDeclaredConstructor().newInstance();

    // Connect with user name and password
    info("Connect with user name and password");
    XQConnection xqjc = xqds.getConnection("admin", "admin");

    // Close the connection
    info("Close successful connection");
    xqjc.close();
  }
}

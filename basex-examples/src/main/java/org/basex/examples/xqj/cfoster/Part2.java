package org.basex.examples.xqj.cfoster;

import java.io.*;

import javax.xml.xquery.*;

/**
 * XQJ Example, derived from the XQJ Tutorial
 * <a href="http://www.cfoster.net/articles/xqj-tutorial">
 * http://www.cfoster.net/articles/xqj-tutorial</a> from Charles Foster.
 *
 * Part 2: Executing XQuery in Java.
 *
 * @author BaseX Team 2005-14, BSD License
 */
public final class Part2 extends Main {
  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    init("2: Executing XQuery in Java");

    // Create the connection
    XQConnection conn = connect();

    // Return book titles from 'books.xml'
    info("Return book titles from 'books.xml'");

    String path = new File("src/main/resources/xml").getAbsolutePath();
    String xqueryString =
      "for $x in doc('" + path + "/books.xml')//book " +
      "return $x/title/text()";

    XQExpression xqe = conn.createExpression();
    XQResultSequence rs = xqe.executeQuery(xqueryString);
    while(rs.next())
      System.out.println(rs.getItemAsString(null));

    // Get book prices
    info("Get book prices");

    xqueryString =
      "for $x in doc('" + path + "/books.xml')//book " +
      "return xs:float($x/price)";

    rs = xqe.executeQuery(xqueryString);
    while(rs.next()) {
      float price = rs.getFloat();
      System.out.println("price = " + price);
    }
    conn.close();

    // Closing connection to the Database.
    close(conn);
  }
}

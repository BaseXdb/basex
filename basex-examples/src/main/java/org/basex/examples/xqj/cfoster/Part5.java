package org.basex.examples.xqj.cfoster;

import java.io.*;

import javax.xml.namespace.*;
import javax.xml.xquery.*;

/**
 * XQJ Example, derived from the XQJ Tutorial
 * <a href="http://www.cfoster.net/articles/xqj-tutorial">
 * http://www.cfoster.net/articles/xqj-tutorial</a> from Charles Foster.
 *
 * Part 5: Stream massive amounts of XML to XQuery expressions.
 *
 * @author BaseX Team, BSD License
 */
public final class Part5 extends Main {
  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String... args) throws Exception {
    init("5: Stream massive amounts of XML to XQuery expressions");

    // Create the connection
    XQConnection conn = connect();

    // Enable deferred binding
    info("Enable deferred binding");

    // Create a NEW XQStaticContext Object (based on the current static context)
    XQStaticContext properties = conn.getStaticContext();

    // Set its Binding Mode property to deferred (i.e. streaming).
    properties.setBindingMode(XQConstants.BINDING_MODE_DEFERRED);

    XQExpression xqe = conn.createExpression(properties);

    try(InputStream in = new FileInputStream("src/main/resources/xml/factbook.xml")) {
      xqe.bindDocument(new QName("x"), in, null, null);

      String xqueryString = "declare variable $x external; $x//city";
      XQResultSequence rs = xqe.executeQuery(xqueryString);

      int c = 0;
      while(rs.next()) ++c;
      System.out.println(c + " results.");
    }

    // Closing connection to the Database.
    close(conn);
  }
}

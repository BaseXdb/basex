package org.basex.examples.xqj.cfoster;

import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQConstants;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQStaticContext;

/**
 * XQJ Example, derived from the XQJ Tutorial
 * <a href="http://www.cfoster.net/articles/xqj-tutorial">
 * http://www.cfoster.net/articles/xqj-tutorial</a> from Charles Foster.
 *
 * Part 5: Stream massive amounts of XML to XQuery expressions.
 *
 * @author BaseX Team 2005-12, BSD License
 */
public final class Part5 extends Main {
  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
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

    String surl = "http://www.w3.org/TR/2007/REC-xquery-20070123/xquery.xml";
    URL url = new URL(surl);
    xqe.bindDocument(new QName("x"), url.openStream(), null, null);

    String xqueryString = "declare variable $x external; $x//p";
    XQResultSequence rs = xqe.executeQuery(xqueryString);

    int c = 0;
    while(rs.next()) ++c;
    System.out.println(c + " results.");

    // Closing connection to the Database.
    close(conn);
  }
}

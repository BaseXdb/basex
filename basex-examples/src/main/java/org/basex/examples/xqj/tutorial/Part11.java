package org.basex.examples.xqj.tutorial;

import java.io.*;

import javax.xml.namespace.*;
import javax.xml.transform.stream.*;
import javax.xml.xquery.*;

/**
 * XQJ Example, derived from the XQJ Tutorial
 * <a href="http://www.xquery.com/tutorials/xqj_tutorial">
 * http://www.xquery.com/tutorials/xqj_tutorial</a>
 * from Marc van Cappellen.
 *
 * Part 11: Processing Large Inputs.
 *
 * @author BaseX Team 2005-13, BSD License
 */
public final class Part11 extends Main {
  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    init("11: Processing Large Inputs");

    // Create a connection
    XQConnection xqc = connect();

    // Pipeline large inputs
    info("Pipeline large inputs");

    XQStaticContext xqsc = xqc.getStaticContext();
    xqc.setStaticContext(xqsc);

    String path = new File("src/main/resources/xml").getAbsolutePath();
    XQExpression xqe = xqc.createExpression();
    XQSequence xqs = xqe.executeQuery(
        "doc('" + path + "/orders.xml')//order");
    XQExpression xqe2 = xqc.createExpression();
    xqe2.bindSequence(new QName("orders"), xqs);

    XQSequence xqs2 = xqe2.executeQuery(
        "declare variable $orders as element() external; " +
        "for $order in $orders where $order/@status = 'closed' " +
        "return <closed_order id = '{$order/@id}'>{ " +
        " $order/* }</closed_order>");
    xqs2.writeSequence(System.out, null);
    xqe2.close();
    xqe.close();
    System.out.println();

    // Stream large inputs
    info("Stream large inputs");

    xqe = xqc.createExpression();
    xqe.bindDocument(XQConstants.CONTEXT_ITEM,
        new StreamSource(path + "/orders.xml"), null);

    xqs = xqe.executeQuery("/orders/order");
    xqs.writeSequence(System.out, null);

    System.out.println();

    // Close the connection
    close(xqc);
  }
}

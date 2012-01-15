package org.basex.examples.xqj.tutorial;

import java.math.BigDecimal;
import javax.xml.namespace.QName;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQSequence;

/**
 * XQJ Example, derived from the XQJ Tutorial
 * <a href="http://www.xquery.com/tutorials/xqj_tutorial">
 * http://www.xquery.com/tutorials/xqj_tutorial</a>
 * from Marc van Cappellen.
 *
 * Part 9: Creating XDM Instances.
 *
 * @author BaseX Team 2005-12, BSD License
 */
public final class Part9 extends Main {
  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    init("9: Creating XDM Instances");

    // Create a connection
    XQConnection xqc = connect();

    // Test item instances
    info("Test item instances");
    XQItemType xqt = xqc.createNodeType();
    XQExpression xqe = xqc.createExpression();
    XQSequence xqs = xqe.executeQuery("1, 'hello', <xml/>");

    while(xqs.next()) {
      if(xqs.instanceOf(xqt)) {
        System.out.println("Node: " + xqs.getNode());
      } else {
        System.out.println("Atomic Value: " + xqs.getAtomicValue());
      }
    }

    // Override data types
    info("Override data types");
    xqt = xqc.createAtomicType(XQItemType.XQBASETYPE_SHORT);
    XQPreparedExpression xqp = xqc.prepareExpression(
        "declare variable $v as xs:short external; $v + 1");
    xqp.bindInt(new QName("v"), 22, xqt);
    print("Override default type mappings", xqp);

    // Create decimal item instance
    info("Create decimal item instance");
    XQItem xqi = xqc.createItemFromObject(new BigDecimal("174"), null);
    xqi.writeItem(System.out, null);
    System.out.println();

    // Create item from query result
    info("Create item from query result");
    xqs = xqe.executeQuery("doc('src/main/resources/xml/orders.xml')//order");
    xqs.next();
    xqi = xqc.createItem(xqs.getItem());

    // Close the connection
    close(xqc);

    // Write item after closing connection
    xqi.writeItem(System.out, null);
    System.out.println();
  }
}

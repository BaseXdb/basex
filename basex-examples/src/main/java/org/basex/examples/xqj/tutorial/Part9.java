package org.basex.examples.xqj.tutorial;

import java.io.*;
import java.math.*;

import javax.xml.namespace.*;
import javax.xml.xquery.*;

/**
 * XQJ Examples, derived from an
 * <a href="https://www.progress.com/products/data-integration-suite/data-integration-suite-developer-center/data-integration-suite-tutorials/learning-xquery/introduction-to-the-xquery-api-for-java-xqj-">
 * XQJ online tutorial</a>.
 *
 * Part 9: Creating XDM Instances.
 *
 * @author BaseX Team 2005-21, BSD License
 */
public final class Part9 extends Main {
  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String... args) throws Exception {
    init("9: Creating XDM Instances");

    // Create a connection
    XQConnection xqc = connect();

    // Test item instances
    info("Test item instances");
    String path = new File("src/main/resources/xml").getAbsolutePath();
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
    xqs = xqe.executeQuery("doc('" + path + "/orders.xml')//order");
    xqs.next();
    xqi = xqc.createItem(xqs.getItem());

    // Close the connection
    close(xqc);

    // Write item after closing connection
    xqi.writeItem(System.out, null);
    System.out.println();
  }
}

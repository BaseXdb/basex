package org.basex.examples.xqj.tutorial;

import javax.xml.namespace.*;
import javax.xml.xquery.*;

/**
 * XQJ Example, derived from the XQJ Tutorial
 * <a href="http://www.xquery.com/tutorials/xqj_tutorial">
 * http://www.xquery.com/tutorials/xqj_tutorial</a>
 * from Marc van Cappellen.
 *
 * Part 7: XQuery Type System.
 *
 * @author BaseX Team 2005-15, BSD License
 */
public final class Part7 extends Main {
  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    init("7: XQuery Type System");

    // Create a connection
    XQConnection xqc = connect();

    // Check sequence types
    info("Check sequence types");
    XQExpression xqe = xqc.createExpression();
    XQSequence xqs = xqe.executeQuery("1, 'hello', <xml/>");

    while(xqs.next()) {
      XQItemType xqit = xqs.getItemType();
      if(xqit.getItemKind() == XQItemType.XQITEMKIND_ATOMIC) {
        // Check atomic type
        switch(xqit.getBaseType()) {
          // Check for string
          case XQItemType.XQBASETYPE_INTEGER:
            long l = xqs.getLong();
            System.out.println("Integer: " + l);
            break;
          // Check for integer
          case XQItemType.XQBASETYPE_STRING:
            String s = (String) xqs.getObject();
            System.out.println("String: " + s);
            break;
          // Any other type
          default:
            String a = xqs.getAtomicValue();
            System.out.println("Atomic Value: " + a);
            break;
        }
      } else {
        // Node type
        System.out.println("Node: " + xqs.getNode());
      }
    }

    // Retrieve static result type
    info("Retrieve static result type");
    XQPreparedExpression xqp = xqc.prepareExpression("1 + 2");
    XQSequenceType xqst = xqp.getStaticResultType();
    System.out.println("Type: " + xqst);

    // Retrieve type of external variables
    info("Retrieve type of external variables");
    xqp = xqc.prepareExpression(
        "declare variable $i as xs:integer external; $i+1");
    QName[] variables = xqp.getAllExternalVariables();
    for(final QName v : variables) {
      xqst = xqp.getStaticVariableType(v);
      System.out.println("Variable $" + v + ": " + xqst);
    }

    // Close the connection
    close(xqc);
  }
}

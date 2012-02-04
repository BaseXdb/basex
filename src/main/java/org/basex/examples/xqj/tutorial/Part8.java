package org.basex.examples.xqj.tutorial;

import java.io.FileInputStream;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import org.w3c.dom.Document;

/**
 * XQJ Example, derived from the XQJ Tutorial
 * <a href="http://www.xquery.com/tutorials/xqj_tutorial">
 * http://www.xquery.com/tutorials/xqj_tutorial</a>
 * from Marc van Cappellen.
 *
 * Part 8: Binding External Variables.
 *
 * @author BaseX Team 2005-12, BSD License
 */
public final class Part8 extends Main {
  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    init("8: Binding External Variables");

    // Create a connection
    XQConnection xqc = connect();
    XQPreparedExpression xqp;

    // Bind an item to an external variable
    xqp = xqc.prepareExpression(
      "declare variable $id as xs:integer external; " +
      "doc('src/main/resources/xml/orders.xml')//order[id=$id]");
    xqp.bindObject(new QName("id"), 174, null);
    print("Bind item to external variable", xqp);

    // Bind integers to external variables
    XQItemType xsinteger;
    xsinteger = xqc.createAtomicType(XQItemType.XQBASETYPE_INTEGER);

    xqp = xqc.prepareExpression(
      "declare variable $v1 external; " +
      "declare variable $v2 external; " +
      "$v1 instance of xs:integer, " +
      "$v1 instance of xs:int, " +
      "$v2 instance of xs:integer, " +
      "$v2 instance of xs:int");
    xqp.bindObject(new QName("v1"), 174, null);
    xqp.bindObject(new QName("v2"), 174, xsinteger);
    print("Bind integers to external variables", xqp);

    // Bind atomic values
    xqp = xqc.prepareExpression(
        "declare variable $v1 external; " +
        "declare variable $v2 external; " +
        "declare variable $v3 external; " +
        "($v1, $v2, $v3)");
    xqp.bindAtomicValue(new QName("v1"), "123",
        xqc.createAtomicType(XQItemType.XQBASETYPE_STRING));
    xqp.bindAtomicValue(new QName("v2"), "123",
        xqc.createAtomicType(XQItemType.XQBASETYPE_INTEGER));
    xqp.bindAtomicValue(new QName("v3"), "123",
        xqc.createAtomicType(XQItemType.XQBASETYPE_DOUBLE));

    // Invalid integer cast
    info("Invalid integer cast");
    xqp = xqc.prepareExpression("declare variable $e external; $e");
    try {
      xqp.bindAtomicValue(new QName("e"), "abc",
          xqc.createAtomicType(XQItemType.XQBASETYPE_INTEGER));
    } catch(final XQException ex) {
      System.out.println(ex.getMessage());
    }

    // Invalid item type
    info("Invalid item type:");
    xqp = xqc.prepareExpression("declare variable $e external; $e");
    try {
      xqp.bindAtomicValue(new QName("e"), "123", null);
    } catch(final XQException ex) {
      System.out.println(ex.getMessage());
    }

    // Bind specific types
    xqp = xqc.prepareExpression("declare variable $v external; $v");
    xqp.bindInt(new QName("v"), 123,
        xqc.createAtomicType(XQItemType.XQBASETYPE_INTEGER));
    print("Bind specific types", xqp);

    // Bind via DOM
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder parser = dbf.newDocumentBuilder();
    Document domDocument = parser.parse("src/main/resources/xml/orders.xml");

    xqp = xqc.prepareExpression("declare variable $e external; $e");
    xqp.bindNode(new QName("e"), domDocument, null);
    print("Bind via DOM", xqp);

    // Bind via StAX
    XMLInputFactory xif = XMLInputFactory.newInstance();
    FileInputStream fis = new FileInputStream(
        "src/main/resources/xml/orders.xml");
    XMLStreamReader reader = xif.createXMLStreamReader(fis);

    xqp = xqc.prepareExpression("declare variable $e external; $e");
    xqp.bindDocument(new QName("e"), reader, null);
    fis.close();
    print("Bind via StAX", xqp);

    // Bind via input stream
    xqp = xqc.prepareExpression("declare variable $e external; $e");
    fis = new FileInputStream("src/main/resources/xml/orders.xml");
    xqp.bindDocument(new QName("e"), fis, null, null);
    fis.close();
    print("Bind via input stream", xqp);

    // Close the connection
    close(xqc);
  }
}

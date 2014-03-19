package org.basex.examples.xqj.cfoster;

import java.io.*;
import java.util.*;

import javax.xml.datatype.*;
import javax.xml.namespace.*;
import javax.xml.parsers.*;
import javax.xml.xquery.*;

import org.w3c.dom.*;

/**
 * XQJ Example, derived from the XQJ Tutorial
 * <a href="http://www.cfoster.net/articles/xqj-tutorial">
 * http://www.cfoster.net/articles/xqj-tutorial</a> from Charles Foster.
 *
 * Part 4: XDM Model within XQJ.
 *
 * @author BaseX Team 2005-14, BSD License
 */
public final class Part4 extends Main {
  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    init("4: XDM Model within XQJ");

    // Create the connection
    XQConnection conn = connect();

    // Create XQuery items from int values
    info("Create XQuery items from int values");
    XQItem[] items = new XQItem[7];

    // Create an XQItem type, with a type of xs:int and a value of 0
    items[0] = conn.createItemFromInt(0, null);

    XQItemType xsInteger = conn.createAtomicType(XQItemType.XQBASETYPE_INTEGER);
    XQItemType xsString = conn.createAtomicType(XQItemType.XQBASETYPE_STRING);
    XQItemType xsByte = conn.createAtomicType(XQItemType.XQBASETYPE_BYTE);
    XQItemType xsDecimal = conn.createAtomicType(XQItemType.XQBASETYPE_DECIMAL);
    XQItemType xsLong = conn.createAtomicType(XQItemType.XQBASETYPE_LONG);

    // Create an XQItem, with a type of xs:integer and a value of 1
    items[1] = conn.createItemFromInt(1, xsInteger);

    // Create an XQItem, with a type of xs:string and a value of 2
    items[2] = conn.createItemFromInt(2, xsString);

    // Create an XQItem, with a type of xs:byte and a value of 3
    items[3] = conn.createItemFromInt(3, xsByte);

    // Create an XQItem, with a type of xs:decimal and a value of 4
    items[4] = conn.createItemFromInt(4, xsDecimal);

    // Create an XQItem, with a type of xs:long and a value of 5
    items[5] = conn.createItemFromInt(5, xsLong);

    // Try to create an XQItem, with a type of xs:byte and a value of 1000
    // This causes an XQException, because the
    // value 1000 is outside the range of xs:byte (-128 to 127)
    try {
      items[6] = conn.createItemFromInt(1000, xsByte);
    } catch(final XQException ex) {
      System.out.println(ex.getMessage());
    }

    for(XQItem it : items) {
      if(it != null) it.writeItem(System.out, null);
      System.out.print(' ');
    }
    System.out.println();

    // Create items from atomic values
    info("Create items from atomic values");

    XQItemType date = // xs:date
    conn.createAtomicType(XQItemType.XQBASETYPE_DATE);

    XQItemType hex = // xs:hexBinary
    conn.createAtomicType(XQItemType.XQBASETYPE_HEXBINARY);

    XQItem dateValue = conn.createItemFromAtomicValue("2007-01-23", date);
    XQItem binaryData = conn.createItemFromAtomicValue("48656C6C6F", hex);

    dateValue.writeItem(System.out, null);
    System.out.println();
    binaryData.writeItem(System.out, null);
    System.out.println();

    // Create items from Java objects
    info("Create items from atomic values");
    items = new XQItem[3];

    // Create an XQItem with a type of xs:int
    items[0] = conn.createItemFromObject(5, null);

    // Create an XQItem with a type of xs:float
    items[1] = conn.createItemFromObject(123.4f, null);

    // Create an XQItem with a type of xs:hexBinary
    items[2] = conn.createItemFromObject(new byte[] { 1, 2, 3, 4 }, null);

    for(XQItem it : items) {
      if(it != null) System.out.println(it.getAtomicValue());
    }

    // Create and bind XQuery sequences
    info("Create and bind XQuery sequences");

    List<Object> list = new ArrayList<>();
    list.add(conn.createItemFromInt(1, null));
    list.add(conn.createItemFromInt(2, null));
    list.add(conn.createItemFromInt(3, null));
    list.add(4);
    list.add(5);
    list.add(6);

    XQSequence sequence = conn.createSequence(list.iterator());

    XQPreparedExpression xqpe =
      conn.prepareExpression("declare variable $x as xs:int+ external; $x");

    xqpe.bindSequence(new QName("x"), sequence);

    XQResultSequence rs = xqpe.executeQuery();

    while(rs.next())
      System.out.println(rs.getItemAsString(null) + ", " + rs.getItemType());

    // Bind XQResultSequences to XQuery Expressions
    info("Bind XQResultSequences to XQuery Expressions");

    XQExpression expr = conn.createExpression();

    String path = new File("src/main/resources/xml").getAbsolutePath();
    String xqueryString =
      "for $x in doc('" + path + "/books.xml')//book/@isbn " +
      "return xs:string($x)";

    rs = expr.executeQuery(xqueryString);

    // Create a copy of the XQResultSequence that is scrollable and in memory.
    sequence = conn.createSequence(rs);

    expr.bindSequence(new QName("isbnCodes"), sequence);
    xqueryString = "declare variable $isbnCodes external; $isbnCodes";
    rs = expr.executeQuery(xqueryString);
    while(rs.next()) {
      System.out.println(rs.getItemAsString(null));
    }

    // Retrieve XML nodes
    info("Retrieve XML nodes");

    expr = conn.createExpression();
    xqueryString = "doc('" + path + "/books.xml')//book";
    rs = expr.executeQuery(xqueryString);
    while(rs.next()) {
      Node book = rs.getNode(); // org.w3c.dom.Element
      System.out.println(book);
    }

    // Create XML nodes
    info("Create XML nodes");

    // Create {@link Document} instance
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document docObj = builder.newDocument();
    Element elementObj = docObj.createElement("e"); // from org.w3c.dom

    // Create an XQItem with a type of element()
    XQItem item1 = conn.createItemFromObject(elementObj, null);

    // Create an XQItem with a type of document-node()
    XQItem items2 = conn.createItemFromObject(docObj, null);

    System.out.println(item1.getItemType());
    System.out.println(items2.getItemType());

    // Retrieve date values
    info("Retrieve date values");

    xqueryString = "for $x in doc('" + path + "/books.xml')//publish_date " +
        "return xs:date($x)";

    rs = expr.executeQuery(xqueryString);

    while(rs.next()) {
      XMLGregorianCalendar cal = (XMLGregorianCalendar) rs.getObject();
      int day   = cal.getDay();
      int month = cal.getMonth();
      int year  = cal.getYear();
      System.out.println(year + "/" + month + '/' + day);
    }

    // Closing connection to the Database.
    close(conn);
  }
}

package org.basex.examples.xqj.tutorial;

import java.io.*;
import java.math.*;

import javax.xml.stream.*;
import javax.xml.xquery.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * XQJ Examples, derived from an
 * <a href="https://www.progress.com/products/data-integration-suite/data-integration-suite-developer-center/data-integration-suite-tutorials/learning-xquery/introduction-to-the-xquery-api-for-java-xqj-">
 * XQJ online tutorial</a>.
 *
 * Part 4: Processing Results.
 *
 * @author BaseX Team 2005-21, BSD License
 */
public final class Part4 extends Main {
  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String... args) throws Exception {
    init("4: Processing Results");

    // Create a connection
    XQConnection xqc = connect();
    XQExpression xqe = xqc.createExpression();

    // Iterate through query results
    info("Iterate through query results");

    String path = new File("src/main/resources/xml").getAbsolutePath();
    XQSequence xqs = xqe.executeQuery(
        "doc('" + path + "/employees.xml')//employee");
    while(xqs.next()) {
      Element employee = (Element) xqs.getObject();
      System.out.println(employee);
    }

    // Iterate through numeric values
    info("Iterate through numeric values");

    xqs = xqe.executeQuery("doc('" + path + "/orders.xml')" +
      "/orders/order/xs:decimal(total_price)");
    while(xqs.next()) {
      BigDecimal price = (BigDecimal) xqs.getObject();
      System.out.println(price);
    }

    // Print atomic values
    info("Print atomic values");

    xqs = xqe.executeQuery("'Hello world!', 123, 1E1, xs:QName('abc')");
    while(xqs.next()) {
      System.out.println(xqs.getAtomicValue());
    }

    // Return single items via SAX
    info("Return single items via SAX");

    ContentHandler ch = new DefaultHandler() {
      @Override
      public void characters(final char[] c, final int s, final int l) {
        System.out.println("Characters/SAX: '" + new String(c, s, l) + '\'');
      }
    };
    xqs = xqe.executeQuery(
        "doc('" + path + "/employees.xml')//employee");
    while(xqs.next()) {
      xqs.writeItemToSAX(ch);
    }

    // Return sequence via SAX
    info("Return sequence via SAX");

    xqs = xqe.executeQuery("doc('" + path + "/employees.xml')//employee");
    xqs.writeSequenceToSAX(ch);

    // Return single items via StAX
    info("Return single items via StAX");

    xqs = xqe.executeQuery("doc('" + path + "/employees.xml')//employee");
    XMLStreamReader xmlReader = xqs.getSequenceAsStream();
    while(true) {
      int type = xmlReader.next();

      if(type == XMLStreamConstants.CHARACTERS) {
        System.out.println("Characters/StAX: '" + xmlReader.getText());
      } else if(type == XMLStreamConstants.END_DOCUMENT) {
        break;
      }
    }

    // Close the connection
    close(xqc);
  }
}

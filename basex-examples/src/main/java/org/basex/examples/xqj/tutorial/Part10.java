package org.basex.examples.xqj.tutorial;

import java.io.*;

import javax.xml.namespace.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;
import javax.xml.xquery.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * XQJ Example, derived from the XQJ Tutorial
 * <a href="http://www.xquery.com/tutorials/xqj_tutorial">
 * http://www.xquery.com/tutorials/xqj_tutorial</a>
 * from Marc van Cappellen.
 *
 * Part 10: XML Pipelines.
 *
 * @author BaseX Team 2005-13, BSD License
 */
public final class Part10 extends Main {
  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    init("10: XML Pipelines");

    // Create a connection
    XQConnection xqc = connect();

    // Pipeline XQuery expressions
    String path = new File("src/main/resources/xml").getAbsolutePath();
    XQExpression xqe = xqc.createExpression();
    XQSequence xqs = xqe.executeQuery("doc('" + path + "/orders.xml')//order");
    XQExpression xqe2 = xqc.createExpression();
    xqe2.bindSequence(new QName("orders"), xqs);

    XQSequence xqs2 = xqe2.executeQuery(
      "declare variable $orders as element() external; " +
      "for $order in $orders where $order/@status = 'closed' " +
      "return <closed_order id = '{$order/@id}'>{ " +
      " $order/* }</closed_order>");

    print("Pipeline XQuery expressions", xqs2);
    xqe2.close();
    xqe.close();

    // Passing XSLT results to XQuery
    info("Passing XSLT results to XQuery");

    // Build an XMLFilter for the XSLT transformation
    SAXTransformerFactory stf = (SAXTransformerFactory)
      TransformerFactory.newInstance();
    XMLFilter xmlf = stf.newXMLFilter(new StreamSource("" + path + "/orders.xsl"));

    // Create a SAX source, the input for the XSLT transformation
    SAXSource saxSource = new SAXSource(xmlf, new InputSource("" + path + "/orders.xml"));

    // Create an XQuery expression
    XQPreparedExpression xqp = xqc.prepareExpression(
        "declare variable $var external; <result>{ $var }</result>");

    // Bind the input document to prepared expression
    xqp.bindDocument(new QName("var"), saxSource, null);

    // Execute the query and print results
    xqs = xqp.executeQuery();
    xqs.writeSequenceToResult(new StreamResult(System.out));
    System.out.println();

    // Passing XQuery results to XSLT
    info("Passing XQuery results to XSLT");

    // Create an XQuery expression
    xqp = xqc.prepareExpression("doc('" + path + "/orders.xml')");
    // Create an XQJFilter
    XQJFilter xqjf = new XQJFilter(xqp);

    // Create an XMLFilter for the XSLT transformation, the 2nd stage
    stf = (SAXTransformerFactory) TransformerFactory.newInstance();
    xmlf = stf.newXMLFilter(
    new StreamSource(path + "/orders.xsl"));
    xmlf.setParent(xqjf);

    // Make sure to capture the SAX events as result of the pipeline
    xmlf.setContentHandler(new DefaultHandler());
    // Activate the pipeline
    xmlf.parse(new InputSource());

    // Close the connection
    close(xqc);
  }

  /**
   * XQuery for Java filter.
   */
  private static class XQJFilter extends XMLFilterImpl {
    /** Prepared expression. */
    final XQPreparedExpression expression;

    /**
     * Constructor.
     * @param xqp prepared expression
     */
    public XQJFilter(final XQPreparedExpression xqp) {
      expression = xqp;
    }

    @Override
    public void parse(final InputSource source) throws SAXException {
      try {
        XQSequence xqs = expression.executeQuery();
        SAXResult result = new SAXResult(getContentHandler());
        xqs.writeSequenceToResult(result);
        xqs.close();
      } catch(XQException ex) {
        throw new SAXException(ex);
      }
    }
  }
}

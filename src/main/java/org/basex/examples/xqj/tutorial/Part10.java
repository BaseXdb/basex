package org.basex.examples.xqj.tutorial;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQSequence;
import org.xml.sax.InputSource;
import org.xml.sax.XMLFilter;

/**
 * XQJ Example, derived from the XQJ Tutorial
 * <a href="http://www.xquery.com/tutorials/xqj_tutorial">
 * http://www.xquery.com/tutorials/xqj_tutorial</a>
 * from Marc van Cappellen.
 *
 * Part 10: XML Pipelines.
 *
 * @author BaseX Team 2005-12, BSD License
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
    XQExpression xqe = xqc.createExpression();
    XQSequence xqs = xqe.executeQuery(
        "doc('src/main/resources/xml/orders.xml')//order");
    XQExpression xqe2 = xqc.createExpression();
    xqe2.bindSequence(new QName("orders"), xqs);

    XQSequence xqs2 = xqe2.executeQuery(
      "declare variable $orders as element(*, xs:untyped) external; " +
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
    XMLFilter xmlf = stf.newXMLFilter(
        new StreamSource("src/main/resources/xml/orders.xsl"));

    // Create a SAX source, the input for the XSLT transformation
    SAXSource saxSource = new SAXSource(xmlf,
        new InputSource("src/main/resources/xml/orders.xml"));

    // Create an XQuery expression
    XQPreparedExpression xqp = xqc.prepareExpression(
        "declare variable $var external; <result>{ $var }</result>");

    // Bind the input document to prepared expression
    xqp.bindDocument(new QName("var"), saxSource, null);

    // Execute the query and print results
    xqs = xqp.executeQuery();
    xqs.writeSequenceToResult(new StreamResult(System.out));
    System.out.println();

    /* Passing XQuery results to XSLT
     * [CG] XQJ: to be checked
    info("Passing XQuery results to XSLT");

    // Create an XQuery expression
    xqp = xqc.prepareExpression("doc('src/main/resources/xml/orders.xml')");
    // Create an XQJFilter
    XQJFilter xqjf = new XQJFilter(xqp);

    // Create an XMLFilter for the XSLT transformation, the 2nd stage
    stf = (SAXTransformerFactory) TransformerFactory.newInstance();
    xmlf = stf.newXMLFilter(
    new StreamSource("src/main/resources/xml/orders.xsl"));
    xmlf.setParent(xqjf);

    // Make sure to capture the SAX events as result of the pipeline
    xmlf.setContentHandler(new DefaultHandler());
    // Activate the pipeline
    xmlf.parse(new InputSource());
    */

    // Close the connection
    close(xqc);
  }

  /**
   * XQuery for Java filter.
  private static class XQJFilter extends XMLFilterImpl {
    /** Prepared expression.
    final XQPreparedExpression expression;

    /**
     * Constructor.
     * @param xqp prepared expression
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
*/
}

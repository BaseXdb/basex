package org.basex.examples.xqj.tutorial;

import java.io.*;

import javax.xml.namespace.*;
import javax.xml.parsers.*;
import javax.xml.xquery.*;

import org.w3c.dom.*;

/**
 * XQJ Example, derived from the XQJ Tutorial
 * <a href="http://www.xquery.com/tutorials/xqj_tutorial">
 * http://www.xquery.com/tutorials/xqj_tutorial</a>
 * from Marc van Cappellen.
 *
 * Part 3: Querying Data from XML Files or Java XML APIs.
 *
 * @author BaseX Team 2005-13, BSD License
 */
public final class Part3 extends Main {
  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    init("3: Querying Data from XML Files or Java XML APIs");

    // Create a connection
    XQConnection xqc = connect();

    // Create and execute an expression
    String path = new File("src/main/resources/xml").getAbsolutePath();
    XQExpression xqe = xqc.createExpression();
    String query = "doc('" + path + "/orders.xml')//order[id='174']";
    XQSequence xqs = xqe.executeQuery(query);
    print("Query: " + query, xqs);

    // Create and execute a second expression
    query = "doc('" + path + "/orders.xml')//order[id='267']";
    xqs = xqe.executeQuery(query);
    if(xqs.next()) print("Query: " + query, xqs);

    // Prepare an expression
    query = "declare variable $id as xs:string external; " +
      "doc('" + path + "/orders.xml')//order[id=$id]";
    XQPreparedExpression xqp = xqc.prepareExpression(query);

    // Bind a variable and execute the query
    xqp.bindString(new QName("id"), "174", null);
    xqs = xqp.executeQuery();
    if(xqs.next()) print("Prepared query, $id=\"174\":", xqs);

    // Bind a second variable and execute the query
    xqp.bindString(new QName("id"), "267", null);
    xqs = xqp.executeQuery();
    if(xqs.next()) print("Prepared query, $id=\"267\":", xqs);

    // Create {@link Document} instance
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document dom = builder.parse(path + "/orders.xml");

    // Bind document to context item
    xqe = xqc.createExpression();
    xqe.bindNode(XQConstants.CONTEXT_ITEM, dom, null);

    // Execute query
    query = ".//order[id='174']";
    xqs = xqe.executeQuery(query);
    if(xqs.next()) print("Query: " + query, xqs);

    // Execute a query from a file input stream
    InputStream is = new FileInputStream(path + "/orders.xq");
    xqs = xqe.executeQuery(is);
    if(xqs.next()) print("Query from input stream", xqs);
    is.close();

    // Close the connection
    close(xqc);
  }
}

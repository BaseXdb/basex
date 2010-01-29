package org.basex.test.examples;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;

/**
 * This class serves an example for executing XQuery requests
 * using the XQuery for Java (XQJ) API.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author BaseX Team
 */
public final class XQJQuery {
  /** Database driver. */
  private static final String DRIVER = "org.basex.api.xqj.BXQDataSource";
  /** Sample query. */
  private static final String QUERY = "doc('etc/xml/input.xml')//li";

  /** Private constructor. */
  private XQJQuery() { }

  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    // Build a connection to the specified driver.
    final XQConnection conn = ((XQDataSource) Class.forName(DRIVER).
        newInstance()).getConnection();

    // Prepare the expression with the document and the query.
    final XQPreparedExpression expr = conn.prepareExpression(QUERY);

    // Execute the query.
    final XQResultSequence result = expr.executeQuery();

    // Get all results of the execution.
    while(result.next()) {
      // Print the results to the console.
      System.out.println(result.getItemAsString(null));
    }

    // Close the expression.
    expr.close();
  }
}

package org.basex.examples.xqj;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;

/**
 * This class serves as an example for executing XQuery requests
 * with the XQuery for Java (XQJ) API.
 *
 * @author BaseX Team 2005-11, BSD License
 */
public final class XQJQuery {
  /** Database driver. */
  private static final String DRIVER = "org.basex.api.xqj.BXQDataSource";
  /** Sample query. */
  private static final String QUERY =
      "doc('src/main/resources/xml/input.xml')//li";

  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {

    System.out.println("=== XQJQuery ===");

    System.out.println("\n* Run query via XQJ:");

    // Build a connection to the specified driver.
    XQConnection conn = ((XQDataSource) Class.forName(DRIVER).
        newInstance()).getConnection();

    // Prepare the expression with the document and the query.
    XQPreparedExpression expr = conn.prepareExpression(QUERY);

    // Execute the query.
    XQResultSequence result = expr.executeQuery();

    // Get all results of the execution.
    while(result.next()) {
      // Print the results to the console.
      System.out.println(result.getItemAsString(null));
    }

    // Close the expression.
    expr.close();
  }
}

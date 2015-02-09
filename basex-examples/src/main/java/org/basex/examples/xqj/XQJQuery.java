package org.basex.examples.xqj;

import javax.xml.xquery.*;

/**
 * This class serves as an example for executing XQuery requests
 * with the XQuery for Java (XQJ) API.
 *
 * @author BaseX Team 2005-15, BSD License
 */
public final class XQJQuery {
  /** Database driver. */
  private static final String DRIVER = "net.xqj.basex.BaseXXQDataSource";

  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {

    System.out.println("=== XQJQuery ===");

    System.out.println("\n* Run query via XQJ:");

    // Create a new data source.
    XQDataSource source = (XQDataSource) Class.forName(DRIVER).newInstance();

    // Build a connection to the specified driver.
    XQConnection conn = source.getConnection("admin", "admin");

    // Prepare a query
    String input = System.getProperty("user.dir") + "/src/main/resources/xml/input.xml";
    XQPreparedExpression expr = conn.prepareExpression("doc('" + input + "')//li");

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

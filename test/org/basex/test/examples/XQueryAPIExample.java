package org.basex.test.examples;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;

/**
 * This class serves an example for executing XQuery requests using the XQuery API.
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author BaseX-Group
 */
public final class XQueryAPIExample {
  /** Database Driver. */
  private static final String DRIVER = "org.basex.api.xqj.BXQDataSource";
  /** Name of the referenced Document. */
  private static final String DOC = "doc('input')";
  /** Sample query. */
  private static final String QUERY = "//li";


   /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    // Gets the XQConnection for the specified Driver.
    final XQConnection conn = ((XQDataSource) Class.forName(DRIVER)
        .newInstance()).getConnection();
    // Prepares the Expression with the Document and the Query.
    final XQPreparedExpression expr = conn.prepareExpression(DOC + QUERY);
    // Executes the XQuery query.
    final XQResultSequence result = expr.executeQuery();
    // Gets all results of the execution.
    while(result.next()) {
      // Prints the results to the console.
      System.out.println(result.getItemAsString(null));
    }
  }
}

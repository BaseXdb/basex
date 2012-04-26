package org.basex.examples.xqj.tutorial;

import javax.xml.xquery.*;

/**
 * XQJ Example, derived from the XQJ Tutorial
 * <a href="http://www.xquery.com/tutorials/xqj_tutorial">
 * http://www.xquery.com/tutorials/xqj_tutorial</a>
 * from Marc van Cappellen.
 *
 * Part 6: Manipulating Static Context.
 *
 * @author BaseX Team 2005-12, BSD License
 */
public final class Part6 extends Main {
  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    init("6: Manipulating Static Context");

    // Create a connection
    XQConnection xqc = connect();

    // Set space policy to PRESERVE
    XQStaticContext xqsc = xqc.getStaticContext();
    xqsc.setBoundarySpacePolicy(XQConstants.BOUNDARY_SPACE_PRESERVE);
    xqc.setStaticContext(xqsc);

    // Execute query
    XQPreparedExpression xqp = xqc.prepareExpression("<e> </e>");
    print("Set space policy to PRESERVE", xqp);

    // Change space policy to STRIP
    xqsc.setBoundarySpacePolicy(XQConstants.BOUNDARY_SPACE_STRIP);
    xqc.setStaticContext(xqsc);

    // the boundary-space policy for this second query *is* preserve
    xqp = xqc.prepareExpression("<e> </e>");
    print("Change space policy to STRIP", xqp);

    // Close the connection
    close(xqc);
  }
}

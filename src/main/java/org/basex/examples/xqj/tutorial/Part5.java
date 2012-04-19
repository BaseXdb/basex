package org.basex.examples.xqj.tutorial;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQSequence;

/**
 * XQJ Example, derived from the XQJ Tutorial
 * <a href="http://www.xquery.com/tutorials/xqj_tutorial">
 * http://www.xquery.com/tutorials/xqj_tutorial</a>
 * from Marc van Cappellen.
 *
 * Part 5: Serializing Results.
 *
 * @author BaseX Team 2005-12, BSD License
 */
public final class Part5 extends Main {
  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    init("5: Serializing Results");

    // Create a connection
    XQConnection xqc = connect();
    XQExpression xqe = xqc.createExpression();

    // Serialize output to disk
    info("Serialize output to disk");

    String path = new File("src/main/resources/xml").getAbsolutePath();
    FileOutputStream fos = new FileOutputStream("result.xml");
    XQSequence xqs = xqe.executeQuery(
        "doc('" + path + "/orders.xml')//order[id='174']");
    xqs.writeSequence(fos, new Properties());
    fos.close();

    /* Remaining examples from the tutorial are skipped, as
     * serialization in BaseX uses defaults. */

    // Delete result file
    info("Delete result file");
    new File("result.xml").delete();

    // Close the connection
    close(xqc);
  }
}

package org.basex.examples.xqj.cfoster;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;

/**
 * XQJ Example, derived from the XQJ Tutorial
 * <a href="http://www.cfoster.net/articles/xqj-tutorial">
 * http://www.cfoster.net/articles/xqj-tutorial</a> from Charles Foster.
 *
 * Part 3: Binding Java variables to XQuery.
 *
 * @author BaseX Team 2005-11, BSD License
 */
public final class Part3 extends Main {
  /** Default ISBN number. */
  private static final String DEFAULT_ISBN = "059652112X";

  /**
   * Main method of the example class.
   * @param args (ignored) command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    init("3: Binding Java variables to XQuery");

    // Create the connection
    XQConnection conn = connect();

    // Lookup specific ISBN
    String isbnID = args.length > 0 ? args[0] : DEFAULT_ISBN;
    info("Lookup books by ISBN '" + isbnID + "'");

    XQExpression xqe = conn.createExpression();

    // Bind variable to expression
    xqe.bindString(new QName("userisbn"), isbnID, null);

    String xqueryString =
      "declare variable $userisbn external; " +
      "for $x in doc('src/main/resources/xml/books.xml')//book " +
      "where $x/@isbn = $userisbn " +
      "return $x/title/text()";

    XQResultSequence rs = xqe.executeQuery(xqueryString);
    while(rs.next())
      System.out.println(rs.getItemAsString(null));

    // Lookup by date range
    info("Lookup books by date range");

    xqueryString =
      "declare variable $fromDate as xs:date external; " +
      "declare variable $toDate as xs:date external; " +
      "for $x in doc('src/main/resources/xml/books.xml')//book " +
      "let $publishDate := xs:date($x/publish_date) " +
      "where $publishDate > $fromDate and $publishDate < $toDate " +
      "return $x/title/text()";

    XQPreparedExpression xqpe =
      conn.prepareExpression(xqueryString);

    // xs:date, we can use this to validate against when binding
    XQItemType dateType = conn.createAtomicType(XQItemType.XQBASETYPE_DATE);

    try {
      // validate against dateType (xs:date)
      xqpe.bindAtomicValue(new QName("fromDate"), "2008-01-32", dateType);
      // There are NOT 32 days in January, so this should fail!
    } catch(XQException ex) {
      System.out.println(ex.getMessage());
      // Now set a proper date: validate against dateType (xs:date)
      xqpe.bindAtomicValue(new QName("fromDate"), "2008-01-01", dateType);
    }

    // validate against dateType (xs:date)
    xqpe.bindAtomicValue(new QName("toDate"), "2011-01-01", dateType);

    rs = xqpe.executeQuery();
    while(rs.next())
      System.out.println(rs.getItemAsString(null));

    // Closing connection to the Database.
    close(conn);
  }
}

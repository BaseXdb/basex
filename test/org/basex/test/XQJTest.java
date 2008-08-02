package org.basex.test;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;

/**
 * Test class for the XQuery API.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-07, ISC License
 * @author Andreas Weiler
 */
final class XQJTest {
  /** Driver. */
  static final String DRIVER = "org.basex.api.xqj.BXQDataSource";
  /** Query. */
  static final String QUERY = "doc('/home/db/xml/input.xml')//li";

  /**
   * Starts the xquery module.
   * @throws Exception exception.
   */
  private XQJTest() throws Exception {   
    Class<?> cls = Class.forName(DRIVER);
    // create class instance..
    XQDataSource xqds = (XQDataSource) cls.newInstance();
    XQConnection conn = xqds.getConnection();
    XQPreparedExpression expr = conn.prepareExpression(QUERY);

    // query execution
    XQResultSequence result = expr.executeQuery();

    // output of result sequence
    while(result.next()) {
      System.out.println(result.getItemAsString(null));
      //System.out.println(result.getAtomicValue());
    }

    XQItem item = conn.createItemFromByte((byte) 123,
        conn.createAtomicType(XQItemType.XQBASETYPE_INT));
    System.out.println(item.getInt());
  }
  
  /**
   * Main Method.
   * @param args command line arguments (ignored)
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    new XQJTest();
  }
}

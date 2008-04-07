package org.basex.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQStaticContext;

/**
 * Test for XQuery API.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-07, ISC License
 * @author Andreas Weiler
 */
public final class XQJTest {
  
  /**
   * Starts the xquery module.
   * @param args command line arguments.
   * @throws Exception exception.
   */
  private XQJTest(final String[] args) throws Exception {   

    // <AW> As XML documents can be defined as part of the query,
    // I removed the filename request from here..
    
    // XQuery examples:
    // 1+2
    // (1, 2 + 3, 4 * 5)
    // string(doc('input.xml')/html/body/@bgcolor)
    // for $text in doc('input.xml')//text() order by $text return string($text)

    String query = "";
    if(args.length != 0) {
      for(String a : args) query += a + " ";
    } else {
      System.out.println("Query please:");
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      query = new String(in.readLine());
    }

    // this is an example for declaring the data source without importing
    // a single BaseX specific class in this example. That's how 
    // SQL drivers are often specified with JDBC ("org.postgres..")
    Class cls = Class.forName("org.basex.api.xqj.BXQDataSource");
    
    // create class instance..
    XQDataSource xqds = (XQDataSource) cls.newInstance();
    // the explicit is usually the better choice; it looks like...
    //XQDataSource xqds = new BXQDataSource();

    XQConnection conn = xqds.getConnection();
    XQStaticContext sc = conn.getStaticContext();
    XQPreparedExpression expr = conn.prepareExpression(query, sc);
    
    // query execution
    XQResultSequence result = expr.executeQuery();

    // output of result sequence
    while(result.next()) {
      System.out.println(result.getAtomicValue());
    }
  }
  
  /**
   * Main Method.
   * @param args command line arguments.
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    new XQJTest(args);
  }
}

package org.basex.api.xqj;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * Test for Xquery Api.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-07, ISC License
 * @author Andreas Weiler
 */
public final class Tester{
  
  /**
   * Starts the xquery modul.
   * @throws Exception exception.
   */
  private Tester() throws Exception {   
    
    String str = "";
    String query = "";
    System.out.println("Filename please:"); 
    try {
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    str = new String(in.readLine());
    System.out.println("Query please:");
    query = new String(in.readLine());
    } catch(IOException e) { } 
    
    BXQDataSource xqds = new BXQDataSource();

    BXQConnection conn = xqds.getConnection();
    
    BXQStaticContext sc = conn.getStaticContext();
    
    BXQPreparedExpression expr = conn.prepareExpression(query,
        xqds.getData(str));
    
    expr.executeQuery(sc);
  }
  
  /**
   * Main Method.
   * @param args command line arguments (ignored).
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    new Tester();
  }
}

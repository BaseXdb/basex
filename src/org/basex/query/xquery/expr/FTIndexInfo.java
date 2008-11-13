package org.basex.query.xquery.expr;


/**
 * Container for indexEquivalent determination.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 * @author Sebastin Gath
 */


public class FTIndexInfo {
  /** Flag for optimization of fulltext queries. */
  public static boolean optimize = false;
  /** Flag for sequential prozessing. */
  public boolean seq = true;
  /** Flag for index use. */
  public boolean ui = false;
  /** Number of index results. */
  public int indexSize = 0;
  /** Flag for loading text from disk. */
  public boolean lt = false;
  /** Number of tokens in the query. */
  public int tn = 0;
  
  /**
   * Constructor.
   */
  public FTIndexInfo() {
    seq = false;
    ui = false;
    indexSize = 0;
  }
}

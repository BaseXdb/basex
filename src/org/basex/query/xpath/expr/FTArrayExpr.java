package org.basex.query.xpath.expr;

/**
 * This is an abstract class for fulltext array expressions.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class FTArrayExpr extends ArrayExpr {
  /** Fulltext option. */
  public FTOption fto;
  /** Fulltext position filter. */
  public FTPositionFilter ftpos;
}

package org.basex.query.xpath.expr;

import org.basex.query.FTPos;
import org.basex.query.xpath.values.Num;

/**
 * FTOption; defines options for a fulltext expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTPositionFilter {
  /** Position filter. */
  public FTPos pos;
  /** Window occurrences. */
  public Num window;
  /** Distances. */
  public long[] dist;

  /**
   * Constructor.
   * @param p position filter
   */
  public FTPositionFilter(final FTPos p) {
    pos = p;
  }
  
  @Override
  protected FTPositionFilter clone() {
    try {
      return (FTPositionFilter) super.clone();
    } catch(final CloneNotSupportedException e) {
      return null;
    }
  }
}

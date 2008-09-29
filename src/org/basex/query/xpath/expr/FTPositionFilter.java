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
  /** Flag for loading ft position values. */
  public boolean lp = false;
  /** Flag for saving tokens. */
  public boolean st = false;

  /**
   * Constructor.
   * @param p position filter
   */
  public FTPositionFilter(final FTPos p) {
    pos = p;
    pos.ordered = p.ordered;
    pos.content = p.content;
    pos.same = p.same;
    pos.different = p.different;
  }
}

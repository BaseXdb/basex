package org.basex.query.value.node;

import org.basex.data.*;
import org.basex.query.value.type.*;
import org.basex.util.ft.*;

/**
 * Disk-based full-text Node item.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FTNode extends DBNode {
  /** Length of the full-text token. */
  private final int tl;
  /** Total number of indexed results. */
  private final int is;
  /** Full-text matches. */
  public FTMatches all;

  /**
   * Constructor, called by the sequential variant.
   * @param a matches
   * @param s scoring
   */
  public FTNode(final FTMatches a, final double s) {
    this(a, null, 0, 0, 0, s);
  }

  /**
   * Constructor, called by the index variant.
   * @param a full-text matches
   * @param d data reference
   * @param p pre value
   * @param l token length
   * @param tis number of indexed results
   * @param s score value out of the index
   */
  public FTNode(final FTMatches a, final Data d, final int p, final int l, final int tis,
      final double s) {

    super(d, p, null, NodeType.TXT);
    all = a;
    tl = l;
    is = tis;
    if(s != -1) score = s;
  }

  @Override
  public double score() {
    if(score == null) {
      if(all == null) return 0;
      score = Scoring.textNode(all.size(), is, tl, data.textLen(pre, true));
    }
    return score;
  }

  @Override
  public String toString() {
    return super.toString() + (all != null ? " (" + all.size() + ')' : "");
  }
}

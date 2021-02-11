package org.basex.query.value.node;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.util.ft.*;
import org.basex.query.value.type.*;
import org.basex.util.ft.*;

/**
 * Disk-based full-text Node item.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FTNode extends DBNode {
  /** Length of the full-text token. */
  private final int tl;
  /** Total number of indexed results. */
  private final int is;
  /** Full-text matches. */
  private FTMatches matches;
  /** Score value. */
  private Double score;

  /**
   * Constructor, called by the sequential variant.
   * @param matches matches
   * @param score score value
   */
  public FTNode(final FTMatches matches, final double score) {
    this(matches, null, 0, 0, 0);
    this.score = score;
  }

  /**
   * Constructor, called by the index variant.
   * @param matches full-text matches
   * @param data data reference (can be {@code null})
   * @param pre pre value
   * @param tl token length
   * @param is number of indexed results
   */
  public FTNode(final FTMatches matches, final Data data, final int pre, final int tl,
      final int is) {

    super(data, pre, null, NodeType.TEXT);
    this.matches = matches;
    this.tl = tl;
    this.is = is;
  }

  /**
   * Assigns full-text matches.
   * @param match full-text matches
   */
  public void matches(final FTMatches match) {
    matches = match;
  }

  /**
   * Returns full-text matches.
   * @return full-text matches
   */
  public FTMatches matches() {
    return matches;
  }

  @Override
  public double score() {
    if(score == null) {
      if(matches == null) return 0;
      score = Scoring.textNode(matches.size(), is, tl, data().textLen(pre(), true));
    }
    return score;
  }

  /**
   * Sets a new score value.
   * @param s score value
   */
  public void score(final double s) {
    score = s;
  }

  @Override
  public void plan(final QueryString qs) {
    super.plan(qs);
    if(matches != null) qs.paren(matches.size());
  }
}

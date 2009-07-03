package org.basex.data;

/**
 * Single full-text string match.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class FTStringMatch implements Comparable<FTStringMatch> {
  /** Query position. */
  public byte queryPos;
  /** Start position. */
  public int start;
  /** End position. */
  public int end;
  /** Include/exclude flag. */
  public boolean not;
  /** Gaps (contiguous) flag. */
  public boolean gaps;

  /**
   * Constructor.
   * @param s start position
   * @param e end position
   * @param p query pos
   */
  FTStringMatch(final int s, final int e, final byte p) {
    start = s;
    end = e;
    queryPos = p;
  }

  /**
   * Checks if the match is included in the specified match.
   * @param mtc match to be compared
   * @return result of check
   */
  boolean in(final FTStringMatch mtc) {
    return start >= mtc.start && end <= mtc.end;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append((not ? "-" : "+") + "[" + queryPos + ": ");
    sb.append(start == end ? "" + start : start + "-" + end);
    return sb.append("]").toString();
  }

  public int compareTo(final FTStringMatch sm) {
    final int s = start - sm.start;
    return s != 0 ? s : end - sm.end;
  }
}

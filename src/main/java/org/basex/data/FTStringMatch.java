package org.basex.data;

/**
 * Single full-text string match.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class FTStringMatch implements Comparable<FTStringMatch> {
  /** Query position. */
  public final byte q;
  /** Start position. */
  public final int s;
  /** End position. */
  public int e;
  /** Include/exclude flag. */
  public boolean n;
  /** Gaps (non-contiguous) flag. */
  public boolean g;

  /**
   * Constructor.
   * @param st start position
   * @param en end position
   * @param qp query pos
   */
  FTStringMatch(final int st, final int en, final byte qp) {
    s = st;
    e = en;
    q = qp;
  }

  /**
   * Checks if the match is included in the specified match.
   * @param mtc match to be compared
   * @return result of check
   */
  boolean in(final FTStringMatch mtc) {
    return s >= mtc.s && e <= mtc.e;
  }

  @Override
  public int compareTo(final FTStringMatch sm) {
    final int st = s - sm.s;
    return st != 0 ? st : e - sm.e;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append((n ? "-" : "+") + "[" + q + ": ");
    sb.append(s == e ? "" + s : s + "-" + e);
    return sb.append("]").toString();
  }
}

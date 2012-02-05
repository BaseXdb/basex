package org.basex.data;

/**
 * Single full-text string match.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FTStringMatch implements Comparable<FTStringMatch> {
  /** Query position. */
  public final int q;
  /** Start position. */
  public final int s;
  /** End position. */
  public int e;
  /** Exclude flag. */
  public boolean ex;
  /** Gaps (non-contiguous) flag. */
  public boolean g;

  /**
   * Constructor.
   * @param st start position
   * @param en end position
   * @param qp query pos
   */
  FTStringMatch(final int st, final int en, final int qp) {
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
  public boolean equals(final Object o) {
    if(!(o instanceof FTStringMatch)) return false;
    final FTStringMatch sm = (FTStringMatch) o;
    return s == sm.s && e == sm.e;
  }

  @Override
  public int compareTo(final FTStringMatch sm) {
    final int st = s - sm.s;
    return st != 0 ? st : e - sm.e;
  }

  @Override
  public int hashCode() {
    return s * e;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append((ex ? "-" : "+") + '[' + q + ": ");
    sb.append(s == e ? String.valueOf(s) : s + "-" + e);
    return sb.append(']').toString();
  }
}

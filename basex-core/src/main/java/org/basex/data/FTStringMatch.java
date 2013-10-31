package org.basex.data;

/**
 * Single full-text string match.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class FTStringMatch implements Comparable<FTStringMatch> {
  /** Position of the token in the query. */
  public final int pos;
  /** Start position. */
  public final int start;
  /** End position. */
  public int end;
  /** Exclude flag. */
  public boolean exclude;
  /** Gaps (non-contiguous) flag. */
  public boolean gaps;

  /**
   * Constructor.
   * @param st start position
   * @param en end position
   * @param qp query pos
   */
  FTStringMatch(final int st, final int en, final int qp) {
    start = st;
    end = en;
    pos = qp;
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
  public boolean equals(final Object o) {
    if(!(o instanceof FTStringMatch)) return false;
    final FTStringMatch sm = (FTStringMatch) o;
    return start == sm.start && end == sm.end;
  }

  @Override
  public int compareTo(final FTStringMatch sm) {
    final int s = start - sm.start;
    return s == 0 ? end - sm.end : s;
  }

  @Override
  public int hashCode() {
    final int h = start + 1;
    return (h << 5) - h + end;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder().append(pos);
    sb.append(':').append(start).append('-').append(end);
    return exclude ? "not(" + sb + ')' : sb.toString();
  }
}

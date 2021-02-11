package org.basex.query.util.ft;

/**
 * Single full-text string match.
 *
 * @author BaseX Team 2005-21, BSD License
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
   * @param start start position
   * @param end end position
   * @param pos query pos
   */
  FTStringMatch(final int start, final int end, final int pos) {
    this.start = start;
    this.end = end;
    this.pos = pos;
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
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof FTStringMatch)) return false;
    final FTStringMatch sm = (FTStringMatch) obj;
    return start == sm.start && end == sm.end;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    if(exclude) sb.append("not(");
    sb.append(pos).append(':').append(start).append('-').append(end);
    if(exclude) sb.append(')');
    return sb.toString();
  }
}

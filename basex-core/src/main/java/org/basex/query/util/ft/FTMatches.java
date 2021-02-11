package org.basex.query.util.ft;

import org.basex.util.list.*;

/**
 * AllMatches full-text container, referencing several {@link FTMatch} instances.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FTMatches extends ObjectList<FTMatch, FTMatches> {
  /** Position of a token in the query. */
  public int pos;

  /**
   * Constructor.
   */
  public FTMatches() {
  }

  /**
   * Constructor.
   * @param pos query position
   */
  public FTMatches(final int pos) {
    this();
    this.pos = pos;
  }

  /**
   * Resets the match container.
   * @param ps query position
   */
  public void reset(final int ps) {
    pos = ps;
    size = 0;
  }

  /**
   * Adds a match entry.
   * @param ps position
   */
  public void or(final int ps) {
    or(ps, ps);
  }

  /**
   * Adds a match entry.
   * @param start start position
   * @param end end position
   */
  public void or(final int start, final int end) {
    add(new FTMatch(1).add(new FTStringMatch(start, end, pos)));
  }

  /**
   * Adds a match entry.
   * @param start start position
   * @param end end position
   */
  public void and(final int start, final int end) {
    final FTStringMatch sm = new FTStringMatch(start, end, pos);
    for(final FTMatch m : this) m.add(sm);
  }

  /**
   * Checks if at least one of the matches contains only includes.
   * @return result of check
   */
  public boolean matches() {
    for(final FTMatch m : this) {
      if(m.match()) return true;
    }
    return false;
  }

  /**
   * Combines two matches as phrase.
   * @param all second match list
   * @param distance word distance
   * @return true if matches are left
   */
  public boolean phrase(final FTMatches all, final int distance) {
    int a = 0, b = 0, c = 0;
    while(a < size && b < all.size) {
      final int e = all.list[b].list[0].start;
      final int d = e - list[a].list[0].end - distance;
      if(d == 0) {
        list[c] = list[a];
        list[c++].list[0].end = e;
      }
      if(d >= 0) ++a;
      if(d <= 0) ++b;
    }
    size = c;
    return size != 0;
  }

  @Override
  protected FTMatch[] newArray(final int s) {
    return new FTMatch[s];
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof FTMatches &&
        pos == ((FTMatches) obj).pos && super.equals(obj);
  }
}

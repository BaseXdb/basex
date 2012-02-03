package org.basex.data;

import java.util.Arrays;
import java.util.Iterator;
import org.basex.util.Array;
import org.basex.util.Util;

/**
 * AllMatches full-text container,
 * referencing several {@link FTMatch} instances.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FTMatches implements Iterable<FTMatch> {
  /** Full-text matches. */
  public FTMatch[] match = {};
  /** Number of entries. */
  public int size;
  /** Current number of tokens. */
  public int sTokenNum;

  /**
   * Constructor.
   * @param s sets the token number
   */
  public FTMatches(final int s) {
    reset(s);
  }

  /**
   * Resets the match counter.
   * @param s sets the token number
   */
  public void reset(final int s) {
    sTokenNum = s;
    size = 0;
  }

  /**
   * Adds a match entry.
   * @param p position
   */
  public void or(final int p) {
    or(p, p);
  }

  /**
   * Adds a match entry.
   * @param s start position
   * @param e end position
   */
  public void or(final int s, final int e) {
    add(new FTMatch().add(new FTStringMatch(s, e, sTokenNum)));
  }

  /**
   * Adds a match entry.
   * @param s start position
   * @param e end position
   */
  public void and(final int s, final int e) {
    final FTStringMatch sm = new FTStringMatch(s, e, sTokenNum);
    for(final FTMatch m : this) m.add(sm);
  }

  /**
   * Adds a match entry.
   * @param m match to be added
   */
  public void add(final FTMatch m) {
    if(size == match.length) match = size == 0 ?
        new FTMatch[1] : Arrays.copyOf(match, size << 1);
    match[size++] = m;
  }

  /**
   * Removes the specified match.
   * @param i match offset
   */
  public void delete(final int i) {
    Array.move(match, i + 1, -1, --size - i);
  }

  /**
   * Checks if at least one of the matches contains only includes.
   * @return result of check
   */
  public boolean matches() {
    for(final FTMatch m : this) if(m.match()) return true;
    return false;
  }

  /**
   * Combines two matches as phrase.
   * @param all second match list
   * @param dis word distance
   * @return true if matches are left
   */
  public boolean phrase(final FTMatches all, final int dis) {
    int a = 0, b = 0, c = 0;
    while(a < size && b < all.size) {
      final int e = all.match[b].match[0].s;
      final int d = e - match[a].match[0].e - dis;
      if(d == 0) {
        match[c] = match[a];
        match[c++].match[0].e = e;
      }
      if(d >= 0) ++a;
      if(d <= 0) ++b;
    }
    size = c;
    return size != 0;
  }

  @Override
  public Iterator<FTMatch> iterator() {
    return new Iterator<FTMatch>() {
      private int c = -1;
      @Override
      public boolean hasNext() { return ++c < size; }
      @Override
      public FTMatch next() { return match[c]; }
      @Override
      public void remove() { Util.notexpected(); }
    };
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(Util.name(this) + "[" + sTokenNum + "]");
    for(final FTMatch m : this) sb.append("\n  ").append(m);
    return sb.toString();
  }
}

package org.basex.data;

import java.util.*;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * AllMatches full-text container, referencing several {@link FTMatch} instances.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FTMatches extends ElementList implements Iterable<FTMatch> {
  /** Full-text matches. */
  public FTMatch[] match = {};
  /** Position of a token in the query. */
  public int pos;

  /**
   * Constructor.
   */
  public FTMatches() {
  }

  /**
   * Constructor.
   * @param p query position
   */
  public FTMatches(final int p) {
    pos = p;
  }

  /**
   * Resets the match container.
   * @param p query position
   */
  public void reset(final int p) {
    pos = p;
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
    add(new FTMatch(1).add(new FTStringMatch(s, e, pos)));
  }

  /**
   * Adds a match entry.
   * @param s start position
   * @param e end position
   */
  public void and(final int s, final int e) {
    final FTStringMatch sm = new FTStringMatch(s, e, pos);
    for(final FTMatch m : this) m.add(sm);
  }

  /**
   * Adds a match entry.
   * @param m match to be added
   */
  public void add(final FTMatch m) {
    if(size == match.length) match = Array.copy(match, new FTMatch[Array.newSize(size)]);
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
      final int e = all.match[b].match[0].start;
      final int d = e - match[a].match[0].end - dis;
      if(d == 0) {
        match[c] = match[a];
        match[c++].match[0].end = e;
      }
      if(d >= 0) ++a;
      if(d <= 0) ++b;
    }
    size = c;
    return size != 0;
  }

  @Override
  public Iterator<FTMatch> iterator() {
    return new ArrayIterator<FTMatch>(match, size);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(Util.className(this)).append('[').append(pos).append(']');
    for(final FTMatch m : this) sb.append("\n  ").append(m);
    return sb.toString();
  }
}

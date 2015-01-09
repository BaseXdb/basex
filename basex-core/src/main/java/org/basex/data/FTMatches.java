package org.basex.data;

import java.util.*;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * AllMatches full-text container, referencing several {@link FTMatch} instances.
 *
 * @author BaseX Team 2005-15, BSD License
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
   * @param pos query position
   */
  public FTMatches(final int pos) {
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
   * Adds a match entry.
   * @param ftm match to be added
   */
  public void add(final FTMatch ftm) {
    if(size == match.length) match = Array.copy(match, new FTMatch[Array.newSize(size)]);
    match[size++] = ftm;
  }

  /**
   * Removes the specified match.
   * @param index match index
   */
  public void delete(final int index) {
    Array.move(match, index + 1, -1, --size - index);
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
   * @param distance word distance
   * @return true if matches are left
   */
  public boolean phrase(final FTMatches all, final int distance) {
    int a = 0, b = 0, c = 0;
    while(a < size && b < all.size) {
      final int e = all.match[b].match[0].start;
      final int d = e - match[a].match[0].end - distance;
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
    return new ArrayIterator<>(match, size);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(Util.className(this)).append('[').append(pos).append(']');
    for(final FTMatch m : this) sb.append("\n  ").append(m);
    return sb.toString();
  }
}

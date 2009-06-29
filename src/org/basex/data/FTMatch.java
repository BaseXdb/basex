package org.basex.data;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import org.basex.BaseX;
import org.basex.util.Array;

/**
 * Match full-text container,
 * referencing several {@link FTStringMatch} instances.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class FTMatch implements Iterable<FTStringMatch> {
  /** String matches. */
  FTStringMatch[] match = {};
  /** Number of entries. */
  public int size;

  /**
   * Resets the match.
   */
  public void reset() {
    size = 0;
  }

  /**
   * Adds a all matches of a full-text match.
   * @param mtc match to be added
   * @return self reference
   */
  public FTMatch add(final FTMatch mtc) {
    for(final FTStringMatch m : mtc) add(m);
    return this;
  }

  /**
   * Adds a string match.
   * @param m match to be added
   * @return self reference
   */
  public FTMatch add(final FTStringMatch m) {
    if(size == match.length) {
      match = size == 0 ? new FTStringMatch[1] : Array.extend(match);
    }
    match[size++] = m;
    return this;
  }

  /**
   * Checks if the full-text match is not part of the specified match.
   * @param mtc match to be checked
   * @return result of check
   */
  public boolean notin(final FTMatch mtc) {
    for(final FTStringMatch s : this) { 
      for(final FTStringMatch m : mtc) if(!s.in(m)) return true;
    }
    return false;
  }

  /**
   * Checks if the match contains no string excludes.
   * @return result of check
   */
  boolean match() {
    for(final FTStringMatch s : this) if(s.not) return false;
    return true;
  }

  /**
   * Sorts the matches.
   */
  public void sort() {
    Arrays.sort(match, 0, size, new Comparator<FTStringMatch>() {
      public int compare(final FTStringMatch s1, final FTStringMatch s2) {
        return s1.compareTo(s2);
      }
    });
  }

  public Iterator<FTStringMatch> iterator() {
    return new Iterator<FTStringMatch>() {
      private int c = -1;
      public boolean hasNext() { return ++c < size; }
      public FTStringMatch next() { return match[c]; }
      public void remove() { BaseX.notexpected(); }
    };
  }
  
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName());
    for(final FTStringMatch s : this) sb.append(" " + s);
    return sb.toString();
  }
}

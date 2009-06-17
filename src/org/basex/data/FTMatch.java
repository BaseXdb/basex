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
  FTStringMatch[] match = new FTStringMatch[1];
  /** Number of entries. */
  int size;

  /**
   * Default constructor.
   */
  public FTMatch() { }

  /**
   * Constructor with an initial match.
   * @param mtc match instance
   */
  public FTMatch(final FTMatch mtc) {
    add(mtc);
  }

  /**
   * Constructor with an initial string match.
   * @param sm matches
   */
  FTMatch(final FTStringMatch sm) {
    match[size++] = sm;
  }

  /**
   * Resets the match.
   */
  public void reset() {
    size = 0;
  }

  /**
   * Adds a match entry.
   * @param mtc match to be added
   */
  public void add(final FTMatch mtc) {
    for(int m = 0; m < mtc.size; m++) add(mtc.match[m]);
  }

  /**
   * Adds a match entry.
   * @param m match to be added
   */
  public void add(final FTStringMatch m) {
    if(size == match.length) match = Array.extend(match);
    match[size++] = m;
  }

  /**
   * Checks if no match is included in the specified match.
   * @param mtc match to be checked
   * @return result of check
   */
  public boolean notin(final FTMatch mtc) {
    for(int a = 0; a < size; a++) {
      for(int m = 0; m < mtc.size; m++) {
        if(!match[a].in(mtc.match[m])) return true;
      }
    }
    return false;
  }

  /**
   * Checks if the match contains no string excludes.
   * @return result of check
   */
  boolean match() {
    for(int m = 0; m < size; m++) if(match[m].not) return false;
    return true;
  }

  /**
   * Inverts string includes and excludes.
   */
  void not() {
    for(int m = 0; m < size; m++) match[m].not ^= true;
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
  
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName());
    for(int m = 0; m < size; m++) sb.append(" " + match[m]);
    return sb.toString();
  }

  public Iterator<FTStringMatch> iterator() {
    return new Iterator<FTStringMatch>() {
      private int c = -1;
      public boolean hasNext() { return ++c < size; }
      public FTStringMatch next() { return match[c]; }
      public void remove() { BaseX.notimplemented(); }
    };
  }
}

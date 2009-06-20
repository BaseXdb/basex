package org.basex.data;

import org.basex.util.Array;

/**
 * AllMatches full-text container,
 * referencing several {@link FTMatch} instances.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FTMatches {
  /** Full-text matches. */
  public FTMatch[] match = new FTMatch[1];
  /** Number of entries. */
  public int size;
  /** Current number of tokens. */
  public byte sTokenNum;

  /**
   * Resets the match counter.
   * @param s sets the token number
   */
  public void reset(final byte s) {
    sTokenNum = s;
    size = 0;
  }

  /**
   * Checks if at least one of the matches contains only includes.
   * @return result of check
   */
  public boolean matches() {
    for(int a = 0; a < size; a++) if(match[a].match()) return true;
    return false;
  }

  /**
   * Inverts string includes and excludes.
   */
  public void not() {
    for(int a = 0; a < size; a++) match[a].not();
  }

  /**
   * Merges two matches.
   * @param m1 first match list
   * @param m2 second match list
   * @return self reference
   */
  public static FTMatches or(final FTMatches m1, final FTMatches m2) {
    final FTMatches tmp = new FTMatches();
    for(int a = 0; a < m1.size; a++) tmp.add(m1.match[a]);
    for(int a = 0; a < m2.size; a++) tmp.add(m2.match[a]);
    tmp.sTokenNum = m1.sTokenNum > m2.sTokenNum ? m1.sTokenNum : m2.sTokenNum;
    return tmp;
  }

  /**
   * Merges two matches.
   * @param m1 first match list
   * @param m2 second match list
   * @return self reference
   */
  public static FTMatches and(final FTMatches m1, final FTMatches m2) {
    final FTMatches tmp = new FTMatches();
    for(int a = 0; a < m1.size; a++) {
      for(int b = 0; b < m2.size; b++) {
        final FTMatch m = new FTMatch(m1.match[a]);
        m.add(m2.match[b]);
        tmp.add(m);
      }
    }
    tmp.sTokenNum = m1.sTokenNum > m2.sTokenNum ? m1.sTokenNum : m2.sTokenNum;
    return tmp;
  }

  /**
   * Performs a mild not operation.
   * @param m1 first match list
   * @param m2 second match list
   * @return self reference
   */
  public static FTMatches mildnot(final FTMatches m1, final FTMatches m2) {
    final FTMatches tmp = new FTMatches();
    for(int a = 0; a < m1.size; a++) {
      boolean n = true;
      for(int b = 0; b < m2.size; b++) {
        n &= m1.match[a].notin(m2.match[b]);
      }
      if(n) tmp.add(m1.match[a]);
    }
    return tmp;
  }

  /**
   * Combines two matches as phrase.
   * @param all second match list
   * @return true if matches are left
   */
  public boolean phrase(final FTMatches all) {
    int a = 0, b = 0, c = 0;
    while(a < size && b < all.size) {
      final int e = all.match[b].match[0].start;
      final int d = e - match[a].match[0].end - 1;
      if(d == 0) {
        match[c] = match[a];
        match[c++].match[0].end = e;
      }
      if(d >= 0) a++;
      if(d <= 0) b++;
    }
    size = c;
    return size != 0;
  }

  /**
   * Adds a match entry.
   * @param s position
   */
  public void add(final int s) {
    add(s, s);
  }

  /**
   * Adds a match entry.
   * @param s start position
   * @param e end position
   */
  public void add(final int s, final int e) {
    add(new FTMatch(new FTStringMatch(s, e, sTokenNum)));
  }

  /**
   * Adds a match entry.
   * @param m match to be added
   */
  public void add(final FTMatch m) {
    if(size == match.length) match = Array.extend(match);
    match[size++] = m;
  }

  /**
   * Removes the specified match.
   * @param i match offset
   */
  public void delete(final int i) {
    Array.move(match, i + 1, -1, --size - i);
  }
  
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName() + "[" + sTokenNum + "]");
    for(int m = 0; m < size; m++) sb.append("\n  " + match[m]);
    return sb.toString();
  }
}

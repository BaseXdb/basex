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
  /** Number of entries. */
  public int size;
  /** Full-text matches. */
  public FTMatch[] match = new FTMatch[1];
  /** Current number of tokens. */
  private byte sTokenNum;

  /**
   * Resets the match counter.
   * @param s sets the token number
   */
  public void reset(final byte s) {
    size = 0;
    sTokenNum = s;
  }

  /**
   * Checks if at least one of the matches yields true.
   * @return result of check
   */
  public boolean match() {
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
   * @param all second match list
   */
  public void or(final FTMatches all) {
    for(int a = 0; a < all.size; a++) add(all.match[a]);
    sTokenNum = sTokenNum < all.sTokenNum ? all.sTokenNum : sTokenNum;
  }

  /**
   * Merges two matches.
   * @param all second match list
   * @return self reference
   */
  public FTMatches and(final FTMatches all) {
    final FTMatches tmp = new FTMatches();
    for(int a = 0; a < size; a++) {
      for(int b = 0; b < all.size; b++) {
        final FTMatch m = new FTMatch(match[a]);
        m.add(all.match[b]);
        tmp.add(m);
      }
    }
    tmp.sTokenNum = sTokenNum < all.sTokenNum ? all.sTokenNum : sTokenNum;
    return tmp;
  }

  /**
   * Combines two matches as strings.
   * @param all second match list
   * @return true if matches are left
   */
  public boolean phrase(final FTMatches all) {
    int a = -1, b = -1;
    while(++a < size && ++b < all.size) {
      final int e = all.match[b].match[0].start;
      final int d = e - match[a].match[0].end - 1;
      if(d == 0) {
        match[a].match[0].end = e;
      } else if(d < 0) {
        Array.move(match, a + 1, -1, --size - a--);
      } else {
        b--;
      }
    }
    size = a;
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
    sb.append(getClass().getSimpleName() + "[" + sTokenNum + "]\n");
    for(int m = 0; m < size; m++) {
      if(m != 0) sb.append("\n");
      sb.append("  " + match[m]);
    }
    return sb.toString();
  }
}

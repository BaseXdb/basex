package org.basex.data;

import java.util.Iterator;
import org.basex.BaseX;
import org.basex.util.Array;

/**
 * AllMatches full-text container,
 * referencing several {@link FTMatch} instances.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FTMatches implements Iterable<FTMatch> {
  /** Full-text matches. */
  public FTMatch[] match = {};
  /** Number of entries. */
  public int size;
  /** Current number of tokens. */
  public byte sTokenNum;

  /**
   * Constructor.
   * @param s sets the token number
   */
  public FTMatches(final byte s) {
    reset(s);
  }

  /**
   * Resets the match counter.
   * @param s sets the token number
   */
  public void reset(final byte s) {
    sTokenNum = s;
    size = 0;
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
    add(new FTMatch().add(new FTStringMatch(s, e, sTokenNum)));
  }

  /**
   * Adds a match entry.
   * @param m match to be added
   */
  public void add(final FTMatch m) {
    if(size == match.length) {
      match = size == 0 ? new FTMatch[1] : Array.extend(match);
    }
    match[size++] = m;
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
   * Merges two matches.
   * @param m1 first match list
   * @param m2 second match list
   * @return resulting match
   */
  public static FTMatches or(final FTMatches m1, final FTMatches m2) {
    final FTMatches all = new FTMatches(
      m1.sTokenNum > m2.sTokenNum ? m1.sTokenNum : m2.sTokenNum);
    for(final FTMatch m : m1) all.add(m);
    for(final FTMatch m : m2) all.add(m);
    return all;
  }

  /**
   * Merges two matches.
   * @param m1 first match list
   * @param m2 second match list
   * @return resulting match
   */
  public static FTMatches and(final FTMatches m1, final FTMatches m2) {
    final FTMatches all = new FTMatches(
        m1.sTokenNum > m2.sTokenNum ? m1.sTokenNum : m2.sTokenNum);

    for(final FTMatch s1 : m1) {
      for(final FTMatch s2 : m2) {
        all.add(new FTMatch().add(s1).add(s2));
      }
    }
    return all;
  }

  /**
   * Merges two matches.
   * @param m match
   * @param i position to start from
   * @return resulting match
   */
  public static FTMatches not(final FTMatches m, final int i) {
    final FTMatches all = new FTMatches(m.sTokenNum);
    if(i == m.size) {
      all.add(new FTMatch());
    } else {
      for(final FTStringMatch s : m.match[i]) {
        s.not ^= true;
        for(final FTMatch tmp : not(m, i + 1)) {
          all.add(new FTMatch().add(s).add(tmp));
        }
      }
    }
    return all;
  }

  /**
   * Performs a mild not operation.
   * @param m1 first match list
   * @param m2 second match list
   * @return resulting match, or null if string exclude was found
   */
  public static FTMatches mildnot(final FTMatches m1, final FTMatches m2) {
    final FTMatches all = new FTMatches(m1.sTokenNum);
    for(final FTMatch s1 : m1) {
      //if(!s1.match()) return null;
      boolean n = true;
      for(final FTMatch s2 : m2) {
        //if(!s2.match()) return null;
        n &= s1.notin(s2);
      }
      if(n) all.add(s1);
    }
    return all;
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

  public Iterator<FTMatch> iterator() {
    return new Iterator<FTMatch>() {
      private int c = -1;
      public boolean hasNext() { return ++c < size; }
      public FTMatch next() { return match[c]; }
      public void remove() { BaseX.notexpected(); }
    };
  }
  
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName() + "[" + sTokenNum + "]");
    for(final FTMatch m : this) sb.append("\n  " + m);
    return sb.toString();
  }
}

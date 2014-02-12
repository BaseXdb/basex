package org.basex.query.ft;

import static org.basex.util.Token.*;

import org.basex.util.*;

/**
 * Wildcard expression.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 * @author Dimitar Popov
 */
public final class FTWildcard {
  /** Value encoding the wildcard dot. */
  private static final int DOT = -1;
  /** Original query. */
  private final byte[] query;
  /** Characters. */
  private int[] wc;
  /** Minimum number of occurrence. */
  private int[] min;
  /** Maximum number of occurrence. */
  private int[] max;
  /** Array length. */
  private int size;

  /**
   * Constructor.
   * @param qu query
   */
  public FTWildcard(final byte[] qu) {
    query = qu;
  }

  /**
   * Parses and constructs a new wildcard expression.
   * @return success flag
   */
  public boolean parse() {
    final int[] q = cps(query);
    wc = new int[q.length];
    min = new int[q.length];
    max = new int[q.length];
    size = 0;

    final int ql = q.length;
    for(int qi = 0; qi < ql;) {
      int n = 1;
      int m = 1;
      // parse wildcards
      if(q[qi] == '.') {
        int c = ++qi < ql ? q[qi] : 0;
        // minimum/maximum number of occurrence
        if(c == '?') { // .?
          ++qi;
          n = 0;
          m = 1;
        } else if(c == '*') { // .*
          ++qi;
          n = 0;
          m = Integer.MAX_VALUE;
        } else if(c == '+') { // .+
          ++qi;
          n = 1;
          m = Integer.MAX_VALUE;
        } else if(c == '{') { // .{m,n}
          n = 0;
          m = 0;
          boolean f = false;
          while(true) {
            c = ++qi < ql ? q[qi] : 0;
            if(digit(c)) n = (n << 3) + (n << 1) + c - '0';
            else if(f && c == ',') break;
            else return false;
            f = true;
          }
          f = false;
          while(true) {
            c = ++qi < ql ? q[qi] : 0;
            if(digit(c)) m = (m << 3) + (m << 1) + c - '0';
            else if(f && c == '}') break;
            else return false;
            f = true;
          }
          ++qi;
          if(n > m) return false;
        }
        wc[size] = DOT;
      } else {
        if(q[qi] == '\\' && ++qi == ql) return false;
        wc[size] = q[qi++];
      }
      min[size] = n;
      max[size] = m;
      size++;
    }
    return true;
  }

  /**
   * Returns the maximum length of a potential match.
   * @return {@code true} if a match is found
   */
  public int max() {
    int c = 0;
    for(int s = 0; s < size; s++) {
      final int m = max[s];
      if(m == Integer.MAX_VALUE) return Integer.MAX_VALUE;
      c += m;
    }
    return c;
  }

  /**
   * Returns the wildcard prefix, which is the same for all matches.
   * @return prefix
   */
  public byte[] prefix() {
    final TokenBuilder tb = new TokenBuilder();
    for(int s = 0; s < size && wc[s] != DOT; s++) tb.add(wc[s]);
    return tb.finish();
  }

  /**
   * Checks if the wildcard can match a sub-string in a string.
   * @param t token to search for match
   * @return {@code true} if a match is found
   */
  public boolean match(final byte[] t) {
    return match(cps(t), 0, 0);
  }

  /**
   * Checks if the wildcard can match a sub-string in a string.
   * @param t token to search for match
   * @param tp input position
   * @param qp query position
   * @return {@code true} if a match is found
   */
  private boolean match(final int[] t, final int tp, final int qp) {
    int qi = qp;
    int ti = tp;
    final int tl = t.length;
    while(qi < size) {
      if(wc[qi] == DOT) {
        int n = min[qi];
        final int m = max[qi++];
        // recursively evaluates wildcards (non-greedy)
        while(!match(t, ti + n, qi)) if(ti + ++n > tl) return false;
        if(n > m) return false;
        ti += n;
      } else {
        if(ti >= tl || t[ti++] != wc[qi++]) return false;
      }
    }
    return ti == tl;
  }
}

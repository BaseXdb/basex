package org.basex.query.expr.ft;

import static org.basex.util.Token.*;

import org.basex.util.*;

/**
 * Wildcard expression.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 * @author Dimitar Popov
 */
public final class FTWildcard {
  /** Value encoding the wildcard dot. */
  private static final int DOT = -1;
  /** Simple flag: query contains no wildcard characters. */
  private final boolean simple;
  /** Validity flag. */
  private final boolean valid;

  /** Codepoints. */
  private int[] cps;
  /** Minimum numbers of occurrences. */
  private int[] min;
  /** Maximum numbers of occurrences. */
  private int[] max;
  /** Number of codepoints. */
  private int size;

  /**
   * Constructor.
   * @param token query token
   */
  public FTWildcard(final byte[] token) {
    simple = !contains(token, '.') && !contains(token, '\\');
    valid = parse(token);
  }

  /**
   * Parses and constructs a new wildcard expression.
   * @param token query token
   * @return success flag
   */
  private boolean parse(final byte[] token) {
    final int[] input = cps(token);
    final int ql = input.length;
    cps = new int[ql];
    min = new int[ql];
    max = new int[ql];
    size = 0;

    for(int qi = 0; qi < ql;) {
      int mn = 1, mx = 1;
      // parse wildcards
      if(input[qi] == '.') {
        int c = ++qi < ql ? input[qi] : 0;
        // minimum/maximum number of occurrence
        if(c == '?') { // .?
          ++qi;
          mn = 0;
          mx = 1;
        } else if(c == '*') { // .*
          ++qi;
          mn = 0;
          mx = Integer.MAX_VALUE;
        } else if(c == '+') { // .+
          ++qi;
          mn = 1;
          mx = Integer.MAX_VALUE;
        } else if(c == '{') { // .{m,n}
          mn = 0;
          mx = 0;
          boolean f = false;
          while(true) {
            c = ++qi < ql ? input[qi] : 0;
            if(digit(c)) mn = (mn << 3) + (mn << 1) + c - '0';
            else if(f && c == ',') break;
            else return false;
            f = true;
          }
          f = false;
          while(true) {
            c = ++qi < ql ? input[qi] : 0;
            if(digit(c)) mx = (mx << 3) + (mx << 1) + c - '0';
            else if(f && c == '}') break;
            else return false;
            f = true;
          }
          ++qi;
          if(mn > mx) return false;
        }
        cps[size] = DOT;
      } else {
        if(input[qi] == '\\' && ++qi == ql) return false;
        cps[size] = input[qi++];
      }
      min[size] = mn;
      max[size] = mx;
      size++;
    }
    return true;
  }

  /**
   * Indicates if the wildcard expression was valid.
   * @return result of check
   */
  public boolean simple() {
    return simple;
  }

  /**
   * Returns the maximum byte length of a potential match in the index.
   * @param full support full range of Unicode characters
   * @return maximum length
   */
  public int max(final boolean full) {
    int c = 0;
    for(int s = 0; s < size; s++) {
      final int m = max[s];
      if(m == Integer.MAX_VALUE) return Integer.MAX_VALUE;
      c += full ? cps[s] == DOT ? 4 : cpLength(cps[s]) : m;
    }
    return c;
  }

  /**
   * Returns the wildcard prefix, which is the same for all matches.
   * @return prefix
   */
  public byte[] prefix() {
    final TokenBuilder tb = new TokenBuilder();
    for(int s = 0; s < size && cps[s] != DOT; s++) tb.add(cps[s]);
    return tb.finish();
  }

  /**
   * Checks if the wildcard can match a sub-string in a string.
   * @param tok token to search for match
   * @return {@code true} if a match is found
   */
  public boolean match(final byte[] tok) {
    return match(cps(tok), 0, 0);
  }

  /**
   * Indicates if the input contains no wildcard characters.
   * @return result of check
   */
  public boolean valid() {
    return valid;
  }

  /**
   * Checks if the wildcard can match a sub-string in a string.
   * @param tok token to search for match
   * @param tp input position
   * @param qp query position
   * @return {@code true} if a match is found
   */
  private boolean match(final int[] tok, final int tp, final int qp) {
    final int tl = tok.length;
    int qi = qp, ti = tp;
    while(qi < size) {
      if(cps[qi] == DOT) {
        int n = min[qi];
        final int m = max[qi++];
        // recursively evaluates wildcards (non-greedy)
        while(!match(tok, ti + n, qi)) {
          if(ti + ++n > tl) return false;
        }
        if(n > m) return false;
        ti += n;
      } else {
        if(ti >= tl || tok[ti++] != cps[qi++]) return false;
      }
    }
    return ti == tl;
  }
}

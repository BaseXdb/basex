package org.basex.query.ft;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.util.Arrays;

import org.basex.query.QueryException;
import org.basex.util.InputInfo;

/**
 * Wild-card expression.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 * @author Dimitar Popov
 */
public class FTWildcard {
  /** Value encoding the wild-card dot. */
  private static final int DOT = -1;
  /** Characters. */
  private final int[] wc;
  /** Minimum number of occurrence. */
  private final int[] min;
  /** Maximum number of occurrence. */
  private final int[] max;

  /**
   * Parse and construct a new wild card expression.
   * @param query input wild card expression
   * @param ii input info
   * @throws QueryException {@link org.basex.query.util.Err#FTREG}, if the wild
   * card expression is not valid
   */
  public FTWildcard(final byte[] query, final InputInfo ii)
      throws QueryException {

    final int[] q = cps(query);
    final int[] tmpwc = new int[q.length];
    final int[] tmpmin = new int[q.length];
    final int[] tmpmax = new int[q.length];

    int pos = -1;
    final int ql = q.length;
    for(int qi = 0; qi < ql;) {
      // parse wildcards
      if(q[qi] == '.') {
        int c = ++qi < ql ? q[qi] : 0;
        // minimum/maximum number of occurrence
        int n = 0;
        int m = Integer.MAX_VALUE;
        if(c == '?') { // .?
          ++qi;
          m = 1;
        } else if(c == '*') { // .*
          ++qi;
        } else if(c == '+') { // .+
          ++qi;
          n = 1;
        } else if(c == '{') { // .{m,n}
          m = 0;
          while(true) {
            c = ++qi < ql ? q[qi] : 0;
            if(c >= '0' && c <= '9') n = (n << 3) + (n << 1) + c - '0';
            else if(c == ',') break;
            else FTREG.thrw(ii, query);
          }
          while(true) {
            c = ++qi < ql ? q[qi] : 0;
            if(c >= '0' && c <= '9') m = (m << 3) + (m << 1) + c - '0';
            else if(c == '}') break;
            else FTREG.thrw(ii, query);
          }
          ++qi;
        } else { // .
          m = 1;
          n = 1;
        }
        tmpmin[++pos] = n;
        tmpmax[pos] = m;
        tmpwc[pos] = DOT;
      } else {
        if(q[qi] == '\\' && ++qi == ql) FTREG.thrw(ii, query);
        tmpwc[++pos] = q[qi++];
      }
    }

    if(++pos < tmpwc.length) {
      wc = Arrays.copyOf(tmpwc, pos);
      min = Arrays.copyOf(tmpmin, pos);
      max = Arrays.copyOf(tmpmax, pos);
    } else {
      wc = tmpwc;
      min = tmpmin;
      max = tmpmax;
    }
  }

  /**
   * Check if the wild-card can match a sub-string in a string.
   * @param t token to search for match
   * @return {@code true} if a match is found
   */
  public boolean match(final byte[] t) {
    return match(cps(t), 0, 0);
  }

  /**
   * Check if the wild-card can match a sub-string in a string.
   * @param t token to search for match
   * @param tp input position
   * @param qp query position
   * @return {@code true} if a match is found
   */
  private boolean match(final int[] t, final int tp, final int qp) {
    int qi = qp;
    int ti = tp;
    final int tl = t.length;
    final int wl = wc.length;
    while(qi < wl) {
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

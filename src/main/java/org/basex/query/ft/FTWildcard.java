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
    this(cps(query), ii);
  }

  /**
   * Check if the wild-card can match a sub-string in a string.
   * @param t token to search for match
   * @return <code>true</code> if a match is found
   */
  public boolean match(final byte[] t) {
    return match(cps(t), 0, 0);
  }

  /**
   * Parse a given wild-card query token.
   * @param q query token
   * @param ii input info
   * @throws QueryException {@link org.basex.query.util.Err#FTREG}, if the wild
   * card expression is not valid
   */
  private FTWildcard(final int[] q, final InputInfo ii)
      throws QueryException {

    final int[] tmpwc = new int[q.length];
    final int[] tmpmin = new int[q.length];
    final int[] tmpmax = new int[q.length];

    int pos = -1;
    int ql = 0;
    while(ql < q.length) {
      // parse wildcards
      if(q[ql] == '.') {
        int c = ++ql < q.length ? q[ql] : 0;
        // minimum/maximum number of occurrence
        int n = 0;
        int m = Integer.MAX_VALUE;
        if(c == '?') { // .?
          ++ql;
          m = 1;
        } else if(c == '*') { // .*
          ++ql;
        } else if(c == '+') { // .+
          ++ql;
          n = 1;
        } else if(c == '{') { // .{m,n}
          m = 0;
          while(true) {
            c = ++ql < q.length ? q[ql] : 0;
            if(c >= '0' && c <= '9') n = (n << 3) + (n << 1) + c - '0';
            else if(c == ',') break;
            else FTREG.thrw(ii, q);
          }
          while(true) {
            c = ++ql < q.length ? q[ql] : 0;
            if(c >= '0' && c <= '9') m = (m << 3) + (m << 1) + c - '0';
            else if(c == '}') break;
            else FTREG.thrw(ii, q);
          }
          ++ql;
        } else { // .
          m = 1;
          n = 1;
        }
        tmpmin[++pos] = n;
        tmpmax[pos] = m;
        tmpwc[pos] = DOT;
      } else {
        if(q[ql] == '\\' && ++ql == q.length) FTREG.thrw(ii, q);
        tmpwc[++pos] = q[ql++];
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
   * @param tp input position
   * @param qp query position
   * @return <code>true</code> if a match is found
   */
  private boolean match(final int[] t, final int tp, final int qp) {
    int ql = qp;
    int tl = tp;
    while(ql < wc.length) {
      if(wc[ql] == DOT) {
        int n = min[ql];
        final int m = max[ql++];
        // recursively evaluates wildcards (non-greedy)
        while(!match(t, tl + n, ql))
          if(tl + ++n > t.length) return false;
        if(n > m) return false;
        tl += n;
      } else {
        if(tl >= t.length || t[tl++] != wc[ql++]) return false;
      }
    }
    return tl == t.length;
  }
}

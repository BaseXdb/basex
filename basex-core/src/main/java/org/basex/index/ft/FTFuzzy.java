package org.basex.index.ft;

import java.util.*;

import org.basex.io.random.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.basex.util.similarity.*;

/**
 * Finds all tokens of an index length group that are within a Levenshtein distance of a
 * query token. The tokens are traversed in sorted order: rows of the distance matrix are
 * shared between tokens with a common prefix, and if no completion of a prefix can stay
 * within the distance, all tokens with that prefix are skipped via binary search.
 * The matches are identical to those of {@link Levenshtein#similar(byte[], byte[], int)}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class FTFuzzy {
  /** Infinity (avoids overflows). */
  private static final int INF = Integer.MAX_VALUE / 2;

  /** Token data (see {@link FTIndex}). */
  private final DataAccess dataY;
  /** Normalized codepoints of the query token. */
  private final int[] query;
  /** Maximum number of errors. */
  private final int k;
  /** Reject tokens with fewer than 4 normalized codepoints (dynamic error calculation). */
  private final boolean rejectShort;

  /** Distance matrix; row {@code d} refers to the first {@code d} codepoints of a token. */
  private int[][] rows;
  /** Normalized codepoints of the current token prefix. */
  private final IntList cps = new IntList();

  /**
   * Constructor.
   * @param dataY token data
   * @param token query token
   * @param errors number of allowed errors (dynamic calculation if the value is {@code 0})
   */
  FTFuzzy(final DataAccess dataY, final byte[] token, final int errors) {
    this.dataY = dataY;
    final IntList list = new IntList(token.length);
    Token.forEachCp(token, cp -> FTToken.normalize(cp, list));
    query = list.finish();

    final int ql = query.length;
    if(errors > 0) {
      k = errors;
      rejectShort = false;
    } else {
      // dynamic calculation (see Levenshtein): exact search for short tokens
      k = ql < 4 ? 0 : ql >> 2;
      rejectShort = ql >= 4;
    }
    rows = new int[4][];
    rows[0] = new int[ql + 2];
    for(int c = 0; c <= ql + 1; c++) rows[0][c] = c <= k ? c : INF;
  }

  /**
   * Collects the offsets of all similar tokens in a length group.
   * @param start offset of first entry
   * @param end offset of last entry (exclusive)
   * @param tl token length
   * @return offsets of matching entries
   */
  IntList offsets(final int start, final int end, final int tl) {
    final IntList list = new IntList();
    final int ql = query.length, w = tl + FTIndex.ENTRY;
    // byte offsets and normalized lengths of the codepoint prefixes of the last token
    final int[] offs = new int[tl + 1], depths = new int[tl + 1];
    byte[] last = null;
    int lastCps = 0, p = start;

    while(p < end) {
      final byte[] token = dataY.readBytes(p, tl);
      // reuse the matrix rows of the longest codepoint prefix shared with the last token
      int j = 0;
      if(last != null) {
        int lcp = 0;
        while(lcp < tl && token[lcp] == last[lcp]) ++lcp;
        while(j < lastCps && offs[j + 1] <= lcp) ++j;
      }
      int b = offs[j];
      cps.size(depths[j]);

      // extend the matrix, pruning as soon as no completion can stay within the distance
      boolean alive = true;
      while(alive && b < tl) {
        final int cp = Token.cp(token, b);
        b += Token.cl(token, b);
        final int d = cps.size();
        FTToken.normalize(cp, cps);
        final int ds = cps.size();
        for(int n = d; alive && n < ds; n++) alive = row(n) <= k;
        if(alive) {
          offs[++j] = b;
          depths[j] = ds;
        }
      }
      last = token;
      lastCps = j;

      if(alive) {
        final int d = cps.size();
        if(Math.abs(d - ql) <= k && rows[d][ql] <= k && !(rejectShort && d < 4)) list.add(p);
        p += w;
      } else {
        // skip all tokens that start with the dead prefix
        p = skip(p + w, end, token, b, w);
      }
    }
    return list;
  }

  /**
   * Computes a row of the distance matrix (see {@link Levenshtein#distance(int[], int[], int)}).
   * @param t index of the token codepoint to be processed
   * @return minimum cost of the computed row
   */
  private int row(final int t) {
    final int ql = query.length, lo = Math.max(0, t - k), hi = Math.min(ql - 1, t + k);
    if(t + 1 >= rows.length) rows = Arrays.copyOf(rows, rows.length << 1);
    int[] curr = rows[t + 1];
    if(curr == null) {
      curr = new int[ql + 2];
      rows[t + 1] = curr;
    }
    final int[] prev = rows[t], prev2 = t > 0 ? rows[t - 1] : null;

    final int tn = cps.get(t);
    curr[lo] = lo == 0 ? t + 1 : INF;
    int min = curr[lo];
    for(int c = lo; c <= hi; c++) {
      final int cn = query[c];
      int cost = Math.min(Math.min(prev[c + 1] + 1, curr[c] + 1), prev[c] + (tn == cn ? 0 : 1));
      if(t > 0 && c > 0 && tn == query[c - 1] && cps.get(t - 1) == cn)
        cost = Math.min(cost, prev2[c - 1] + 1);
      curr[c + 1] = cost;
      min = Math.min(min, cost);
    }
    // invalidate the cell after the band; it must not be read as a stale value
    if(hi + 2 <= ql + 1) curr[hi + 2] = INF;
    return min;
  }

  /**
   * Returns the offset of the first token that does not start with the dead prefix.
   * @param from offset of the first candidate
   * @param end offset of last entry (exclusive)
   * @param token current token (its first {@code pl} bytes are the dead prefix)
   * @param pl prefix length
   * @param w width of an index entry
   * @return offset of the first entry with a larger prefix
   */
  private int skip(final int from, final int end, final byte[] token, final int pl, final int w) {
    // gallop, then binary search: cheap for short skips, logarithmic for long ones
    final int e = (end - from) / w;
    int s = 0, step = 1;
    while(s + step <= e && compare(from + (s + step - 1) * w, token, pl) <= 0) {
      s += step;
      step <<= 1;
    }
    int t = Math.min(e, s + step);
    while(s < t) {
      final int m = s + t >>> 1;
      if(compare(from + m * w, token, pl) > 0) t = m;
      else s = m + 1;
    }
    return from + s * w;
  }

  /**
   * Compares the prefix at the specified offset with the prefix of the current token.
   * @param offset offset of the token to compare
   * @param token current token
   * @param pl prefix length
   * @return result of comparison (-1, 0, 1)
   */
  private int compare(final int offset, final byte[] token, final int pl) {
    return Token.compare(dataY.readBytes(offset, pl), 0, pl, token, 0, pl);
  }
}

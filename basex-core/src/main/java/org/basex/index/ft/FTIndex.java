package org.basex.index.ft;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;

import java.io.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.index.stats.*;
import org.basex.index.value.*;
import org.basex.io.random.*;
import org.basex.query.expr.ft.*;
import org.basex.query.util.ft.*;
import org.basex.query.util.index.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;
import org.basex.util.similarity.*;

/**
 * <p>This class provides access to a fuzzy full-text index structure
 * stored on disk. Each token has an entry in sizes, saving its length and a
 * pointer on ftdata, where to find the token and its ftdata.
 * The three database index files start with the prefix
 * {@link DataText#DATAFTX} and have the following format:</p>
 *
 * <ul>
 * <li>File <b>x</b> contains an entry for each token length.
 * Structure: {@code [l, p] ...}.
 * {@code l} is the length of a token [byte].
 * {@code p} is the pointer of the first token with length {@code l} [int].
 * </li>
 * <li>File <b>y</b> contains the tokens and references.
 * Structure: {@code [t0, t1, ... tl, z, s]}
 * {@code t0, t1, ... tl-1} is the token [byte[l]]
 * {@code z} is the pointer on the data entries of the token [long]
 * {@code s} is the number of pre values, saved in data [int]
 * </li>
 * <li>File <b>z</b> contains the {@code id/pos} references.
 *   The values are ordered, but not distinct:
 *   {@code pre1/pos1, pre2/pos2, pre3/pos3, ...} [{@link Num}]</li>
 * </ul>
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FTIndex extends ValueIndex {
  /** Minimum fixed size for each token entry. */
  private static final int ENTRY = 9;

  /** Cached texts. Increases used memory, but speeds up repeated queries. */
  private final IntObjMap<byte[]> ctext = new IntObjMap<>();
  /** Levenshtein reference. */
  private final Levenshtein ls = new Levenshtein();

  /** Index storing each unique token length and pointer
   * on the first token with this length. */
  private final DataAccess dataX;
  /** Index storing each token, its data size and pointer on the data. */
  private final DataAccess dataY;
  /** Storing pre and pos values for each token. */
  private final DataAccess dataZ;

  /** Cache for number of hits and data reference per token. */
  private final IndexCache cache = new IndexCache();
  /** Token positions. */
  private final int[] positions;

  /**
   * Constructor, initializing the index structure.
   * @param data data reference
   * @throws IOException I/O Exception
   */
  public FTIndex(final Data data) throws IOException {
    super(data, IndexType.FULLTEXT);
    // cache token length index
    dataX = new DataAccess(data.meta.dbFile(DATAFTX + 'x'));
    dataY = new DataAccess(data.meta.dbFile(DATAFTX + 'y'));
    dataZ = new DataAccess(data.meta.dbFile(DATAFTX + 'z'));
    positions = new int[data.meta.maxlen + 3];
    final int pl = positions.length;
    for(int p = 0; p < pl; p++) positions[p] = -1;
    for(int is = dataX.readNum(); --is >= 0;) {
      final int p = dataX.readNum();
      positions[p] = dataX.read4();
    }
    positions[pl - 1] = (int) dataY.length();
  }

  @Override
  public synchronized IndexCosts costs(final IndexSearch search) {
    final byte[] token = search.token();
    if(token.length > data.meta.maxlen) return null;

    // estimate costs for queries which stretch over multiple index entries
    final FTOpt opt = ((FTLexer) search).ftOpt();
    return IndexCosts.get(opt.is(FZ) || opt.is(WC) ? Math.max(1, data.meta.size >> 4) :
      entry(token).size);
  }

  @Override
  public synchronized IndexIterator iter(final IndexSearch search) {
    // current search token
    final FTLexer lexer = (FTLexer) search;
    final FTOpt opt = lexer.ftOpt();
    final byte[] token = lexer.token();

    // wildcard search
    if(opt.is(WC)) {
      final FTWildcard wc = new FTWildcard(token);
      if(!wc.valid()) return FTIndexIterator.FTEMPTY;
      if(!wc.simple()) return wildcards(wc, opt.is(DC), token);
    }

    // fuzzy search
    if(opt.is(FZ)) {
      return fuzzy(token, lexer.errors(token));
    }

    // return cached or new result
    final IndexEntry entry = entry(token);
    if(entry.size > 0) {
      return iter(entry.offset, entry.size, dataZ, token);
    }

    // no results
    return FTIndexIterator.FTEMPTY;
  }

  /**
   * Returns a cached index entry.
   * @param token token to be found or cached
   * @return cache entry
   */
  private IndexEntry entry(final byte[] token) {
    final IndexEntry entry = cache.get(token);
    if(entry != null) return entry;

    final long pt = token(token);
    return pt == -1 ? new IndexEntry(token, 0, 0) :
      cache.add(token, size(pt, token.length), pointer(pt, token.length));
  }

  @Override
  public EntryIterator entries(final IndexEntries entries) {
    final byte[] token = entries.token();

    return new EntryIterator() {
      int p = token.length - 1, start, end, nr;
      boolean inner;

      @Override
      public byte[] next() {
        synchronized(FTIndex.this) {
          if(inner && start < end) {
            // loop through all entries with the same character length
            final byte[] entry = dataY.readBytes(start, p);
            if(startsWith(entry, token)) {
              final long poi = dataY.read5();
              nr = dataY.read4();
              if(token.length != 0) cache.add(entry, nr, poi);
              start += p + ENTRY;
              return entry;
            }
          }
          // find next available entry group
          final int pl = positions.length;
          while(++p < pl - 1) {
            start = positions[p];
            if(start == -1) continue;
            int c = p + 1;
            do end = positions[c++]; while(end == -1);
            nr = 0;
            inner = true;
            start = find(token, start, end, p);
            // jump to inner loop
            final byte[] n = next();
            if(n != null) return n;
          }
          // all entries processed: return null
          return null;
        }
      }

      @Override
      public int count() {
        return nr;
      }
    };
  }

  /**
   * Binary search.
   * @param token token to look for
   * @param start start position
   * @param end end position
   * @param ti entry length
   * @return position where the key was found, or would have been found
   */
  private int find(final byte[] token, final int start, final int end, final int ti) {
    final int tl = ti + ENTRY;
    int s = 0, e = (end - start) / tl;
    while(s <= e) {
      final int m = s + e >>> 1, pos = start + m * tl, d = diff(cache(pos, ti), token);
      if(d == 0) return start + m * tl;
      if(d < 0) s = m + 1;
      else e = m - 1;
    }
    return start + s * tl;
  }

  /**
   * Caches the text at the specified position and with the specified length.
   * @param pos position
   * @param ti text length
   * @return text
   */
  private byte[] cache(final int pos, final int ti) {
    // do not cache texts if the fulltext index contains unusually long tokens
    if(ti >= 128) return dataY.readBytes(pos, ti);

    // try to find cached text (requested length may vary in full-text requests)
    final int key = (ti << 24) + pos;
    return ctext.computeIfAbsent(key, () -> dataY.readBytes(pos, ti));
  }

  @Override
  public synchronized byte[] info(final MainOptions options) {
    final TokenBuilder tb = new TokenBuilder();
    final long l = dataX.length() + dataY.length() + dataZ.length();
    tb.add(LI_NAMES).add(data.meta.ftinclude).add(NL);
    tb.add(LI_SIZE).add(Performance.format(l)).add(NL);

    final IndexStats stats = new IndexStats(options.get(MainOptions.MAXSTAT));
    addOccs(stats);
    stats.print(tb);
    return tb.finish();
  }

  @Override
  public boolean drop() {
    return data.meta.drop(DATAFTX + '.');
  }

  @Override
  public synchronized void close() {
    dataX.close();
    dataY.close();
    dataZ.close();
  }

  @Override
  public int size() {
    final int pl = positions.length;
    int size = 0, t = pl - 1;
    while(true) {
      final int e = t;
      while(positions[--t] == -1) {
        if(t == 0) return size;
      }
      size += (positions[e] - positions[t]) / (t + ENTRY);
    }
  }

  /**
   * Determines the pointer on a token.
   * @param token token looking for
   * @return int pointer or {@code -1} if token was not found
   */
  private int token(final byte[] token) {
    final int tl = token.length;
    // left limit
    int s = positions[tl];
    if(s == -1) return -1;

    // find right limit
    int i = 1, e;
    do e = positions[tl + i++]; while(e == -1);
    final int x = e;

    // binary search
    final int o = tl + ENTRY;
    while(s < e) {
      final int m = s + (e - s >> 1) / o * o, d = diff(dataY.readBytes(m, tl), token);
      if(d == 0) return m;
      if(d < 0) s = m + o;
      else e = m - o;
    }
    // accept entry if pointer is inside relevant tokens
    return e != x && s == e && eq(dataY.readBytes(s, tl), token) ? s : -1;
  }

  /**
   * Collects all tokens and their sizes found in the index structure.
   * @param stats statistics
   */
  private void addOccs(final IndexStats stats) {
    int i = 0;
    final int pl = positions.length;
    while(i < pl && positions[i] == -1) ++i;
    int p = positions[i], j = i + 1;
    while(j < pl && positions[j] == -1) ++j;

    final int max = positions[pl - 1];
    while(p < max) {
      final int oc = size(p, i);
      if(stats.adding(oc)) stats.add(dataY.readBytes(p, i), oc);
      p += i + ENTRY;
      if(p == positions[j]) {
        i = j;
        while(j + 1 < pl && positions[++j] == -1);
      }
    }
  }

  /**
   * Gets the pointer on ftdata for a token.
   * @param pt pointer on token
   * @param lt length of the token
   * @return int pointer on ftdata
   */
  private long pointer(final long pt, final int lt) {
    return dataY.read5(pt + lt);
  }

  /**
   * Reads the size of ftdata from disk.
   * @param pt pointer on token
   * @param lt length of the token
   * @return size of the ftdata
   */
  private int size(final long pt, final int lt) {
    return dataY.read4(pt + lt + 5);
  }

  /**
   * Performs a fuzzy search for the specified token with a maximum number of errors.
   * @param token token to look for
   * @param k number of errors allowed
   * @return iterator
   */
  private IndexIterator fuzzy(final byte[] token, final int k) {
    FTIndexIterator iter = FTIndexIterator.FTEMPTY;
    final int tokl = token.length, pl = positions.length, e = Math.min(pl - 1, tokl + k);
    int s = Math.max(1, tokl - k) - 1;
    while(++s <= e) {
      int p = positions[s];
      if(p == -1) continue;
      int t = s + 1, r = -1;
      while(t < pl && r == -1) r = positions[t++];
      while(p < r) {
        if(ls.similar(dataY.readBytes(p, s), token, k)) {
          iter = FTIndexIterator.union(iter(pointer(p, s), size(p, s), dataZ, token), iter);
        }
        p += s + ENTRY;
      }
    }
    return iter;
  }

  /**
   * Performs a wildcard search for the specified token.
   * @param wc wildcard matcher
   * @param full support full range of Unicode characters
   * @param token original search token
   * @return iterator
   */
  private IndexIterator wildcards(final FTWildcard wc, final boolean full, final byte[] token) {
    final IntList pr = new IntList(), ps = new IntList();
    final byte[] prefix = wc.prefix();
    final int pl = positions.length, l = Math.min(pl - 1, wc.max(full));
    for(int p = prefix.length; p <= l; p++) {
      int start = positions[p];
      if(start == -1) continue;
      int c = p + 1, end = -1;
      while(c < pl && end == -1) end = positions[c++];
      start = find(prefix, start, end, p);

      while(start < end) {
        final byte[] t = dataY.readBytes(start, p);
        if(!startsWith(t, prefix)) break;
        if(wc.match(t)) {
          dataZ.cursor(pointer(start, p));
          final int s = size(start, p);
          for(int d = 0; d < s; d++) {
            pr.add(dataZ.readNum());
            ps.add(dataZ.readNum());
          }
        }
        start += p + ENTRY;
      }
    }
    return iter(new FTCache(pr, ps), token);
  }

  /**
   * Returns an iterator for an index entry.
   * @param off offset on entries
   * @param size number of id/pos entries
   * @param da data source
   * @param token index token
   * @return iterator
   */
  private static FTIndexIterator iter(final long off, final int size, final DataAccess da,
      final byte[] token) {
    da.cursor(off);
    final IntList pr = new IntList(size), ps = new IntList(size);
    for(int c = 0; c < size; c++) {
      pr.add(da.readNum());
      ps.add(da.readNum());
    }
    return iter(new FTCache(pr, ps), token);
  }

  /**
   * Returns an iterator for an index entry.
   * @param ftc id cache
   * @param token index token
   * @return iterator
   */
  private static FTIndexIterator iter(final FTCache ftc, final byte[] token) {
    final int size = ftc.pre.size();

    return new FTIndexIterator() {
      final FTMatches all = new FTMatches();
      int pos, pre, c;

      @Override
      public boolean more() {
        if(c == size) return false;
        all.reset(pos);
        pre = ftc.pre.get(ftc.order[c]);
        all.or(ftc.pos.get(ftc.order[c++]));
        while(c < size && pre == ftc.pre.get(ftc.order[c])) {
          all.or(ftc.pos.get(ftc.order[c++]));
        }
        return true;
      }

      @Override
      public FTMatches matches() {
        return all;
      }

      @Override
      public int pre() {
        return pre;
      }

      @Override
      public void pos(final int p) {
        pos = p;
      }

      @Override
      public int size() {
        return size;
      }

      @Override
      public String toString() {
        return Strings.concat(token, '(', size, "x)");
      }
    };
  }

  /**
   * Full-text cache.
   */
  private static final class FTCache {
    /** Order. */
    private final int[] order;
    /** Pre values. */
    private final IntList pre;
    /** Pos values. */
    private final IntList pos;

    /**
     * Constructor.
     * @param pr pre values
     * @param ps positions
     */
    private FTCache(final IntList pr, final IntList ps) {
      final int s = pr.size();
      final long[] v = new long[s];
      for(int i = 0; i < s; i++) v[i] = (long) pr.get(i) << 32 | ps.get(i);
      order = Array.createOrder(v, true);
      pre = pr;
      pos = ps;
    }
  }

  @Override
  public void add(final ValueCache values) {
    throw Util.notExpected();
  }

  @Override
  public void delete(final ValueCache values) {
    throw Util.notExpected();
  }

  @Override
  public void flush() { }
}

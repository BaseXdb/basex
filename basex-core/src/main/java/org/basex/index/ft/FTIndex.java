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
import org.basex.query.util.*;
import org.basex.query.util.ft.*;
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
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class FTIndex extends ValueIndex {
  /** Entry size. */
  private static final int ENTRY = 9;

  /** Cached texts. Increases used memory, but speeds up repeated queries. */
  private final IntObjMap<byte[]> ctext = new IntObjMap<>();
  /** Levenshtein reference. */
  private final Levenshtein ls = new Levenshtein();

  /** Index storing each unique token length and pointer
   * on the first token with this length. */
  private final DataAccess inX;
  /** Index storing each token, its data size and pointer on the data. */
  private final DataAccess inY;
  /** Storing pre and pos values for each token. */
  private final DataAccess inZ;

  /** Cache for number of hits and data reference per token. */
  private final IndexCache cache = new IndexCache();
  /** Token positions. */
  private final int[] tp;

  /**
   * Constructor, initializing the index structure.
   * @param data data reference
   * @throws IOException I/O Exception
   */
  public FTIndex(final Data data) throws IOException {
    super(data, IndexType.FULLTEXT);
    // cache token length index
    inY = new DataAccess(data.meta.dbfile(DATAFTX + 'y'));
    inZ = new DataAccess(data.meta.dbfile(DATAFTX + 'z'));
    inX = new DataAccess(data.meta.dbfile(DATAFTX + 'x'));
    tp = new int[data.meta.maxlen + 3];
    final int tl = tp.length;
    for(int i = 0; i < tl; ++i) tp[i] = -1;
    for(int is = inX.readNum(); --is >= 0;) {
      final int p = inX.readNum();
      tp[p] = inX.read4();
    }
    tp[tl - 1] = (int) inY.length();
  }

  @Override
  public synchronized IndexCosts costs(final IndexToken it) {
    final byte[] tok = it.get();
    if(tok.length > data.meta.maxlen) return null;

    // estimate costs for queries which stretch over multiple index entries
    final FTOpt opt = ((FTLexer) it).ftOpt();
    return IndexCosts.get(opt.is(FZ) || opt.is(WC) ? Math.max(1, data.meta.size >> 4) :
      entry(tok).size);
  }

  @Override
  public synchronized IndexIterator iter(final IndexToken it) {
    final byte[] tok = it.get();

    // wildcard search
    final FTLexer lexer = (FTLexer) it;
    final FTOpt opt = lexer.ftOpt();
    if(opt.is(WC)) return wc(tok);

    // fuzzy search
    if(opt.is(FZ)) return fuzzy(tok, lexer.lserror(tok));

    // return cached or new result
    final IndexEntry e = entry(tok);
    return e.size > 0 ? iter(e.offset, e.size, inZ, tok) : FTIndexIterator.FTEMPTY;
  }

  /**
   * Returns a cache entry.
   * @param token token to be found or cached
   * @return cache entry
   */
  private IndexEntry entry(final byte[] token) {
    final IndexEntry e = cache.get(token);
    if(e != null) return e;

    final long p = token(token);
    return p == -1 ? new IndexEntry(token, 0, 0) :
      cache.add(token, size(p, token.length), pointer(p, token.length));
  }

  @Override
  public EntryIterator entries(final IndexEntries entries) {
    final byte[] prefix = entries.get();
    return new EntryIterator() {
      int ti = prefix.length - 1, i, e, nr;
      boolean inner;

      @Override
      public byte[] next() {
        synchronized(FTIndex.this) {
          if(inner && i < e) {
            // loop through all entries with the same character length
            final byte[] entry = inY.readBytes(i, ti);
            if(startsWith(entry, prefix)) {
              final long poi = inY.read5();
              nr = inY.read4();
              if(prefix.length != 0) cache.add(entry, nr, poi);
              i += ti + ENTRY;
              return entry;
            }
          }
          // find next available entry group
          final int tl = tp.length;
          while(++ti < tl - 1) {
            i = tp[ti];
            if(i == -1) continue;
            int c = ti + 1;
            do e = tp[c++]; while(e == -1);
            nr = 0;
            inner = true;
            i = find(prefix, i, e, ti);
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
    int l = 0, h = (end - start) / tl;
    while(l <= h) {
      final int m = l + h >>> 1;
      final int p = start + m * tl;
      byte[] txt = ctext.get(p);
      if(txt == null) {
        txt = inY.readBytes(p, ti);
        ctext.put(p, txt);
      }
      final int d = diff(txt, token);
      if(d == 0) return start + m * tl;
      if(d < 0) l = m + 1;
      else h = m - 1;
    }
    return start + l * tl;
  }

  @Override
  public synchronized byte[] info(final MainOptions options) {
    final TokenBuilder tb = new TokenBuilder();
    final long l = inX.length() + inY.length() + inZ.length();
    tb.add(LI_NAMES).add(data.meta.ftinclude).add(NL);
    tb.add(LI_SIZE + Performance.format(l) + NL);

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
    inX.close();
    inY.close();
    inZ.close();
  }

  @Override
  public int size() {
    final int tl = tp.length;
    int size = 0, t = tl - 1;
    while(true) {
      final int e = t;
      while(tp[--t] == -1) {
        if(t == 0) return size;
      }
      size += (tp[e] - tp[t]) / (t + ENTRY);
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
    int l = tp[tl];
    if(l == -1) return -1;

    int i = 1;
    int r;
    // find right limit
    do r = tp[tl + i++]; while(r == -1);
    final int x = r;

    // binary search
    final int o = tl + ENTRY;
    while(l < r) {
      final int m = l + (r - l >> 1) / o * o;
      final int c = diff(inY.readBytes(m, tl), token);
      if(c == 0) return m;
      if(c < 0) l = m + o;
      else r = m - o;
    }
    // accept entry if pointer is inside relevant tokens
    return r != x && l == r && eq(inY.readBytes(l, tl), token) ? l : -1;
  }

  /**
   * Collects all tokens and their sizes found in the index structure.
   * @param stats statistics
   */
  private void addOccs(final IndexStats stats) {
    int i = 0;
    final int tl = tp.length;
    while(i < tl && tp[i] == -1) ++i;
    int p = tp[i], j = i + 1;
    while(j < tl && tp[j] == -1) ++j;

    final int max = tp[tl - 1];
    while(p < max) {
      final int oc = size(p, i);
      if(stats.adding(oc)) stats.add(inY.readBytes(p, i), oc);
      p += i + ENTRY;
      if(p == tp[j]) {
        i = j;
        while(j + 1 < tl && tp[++j] == -1);
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
    return inY.read5(pt + lt);
  }

  /**
   * Reads the size of ftdata from disk.
   * @param pt pointer on token
   * @param lt length of the token
   * @return size of the ftdata
   */
  private int size(final long pt, final int lt) {
    return inY.read4(pt + lt + 5);
  }

  /**
   * Performs a fuzzy search for the specified token with a maximum number of errors.
   * @param token token to look for
   * @param k number of errors allowed
   * @return iterator
   */
  private IndexIterator fuzzy(final byte[] token, final int k) {
    FTIndexIterator it = FTIndexIterator.FTEMPTY;
    final int tokl = token.length, tl = tp.length;
    final int e = Math.min(tl - 1, tokl + k);
    int s = Math.max(1, tokl - k) - 1;

    while(++s <= e) {
      int p = tp[s];
      if(p == -1) continue;
      int t = s + 1, r = -1;
      while(t < tl && r == -1) r = tp[t++];
      while(p < r) {
        if(ls.similar(inY.readBytes(p, s), token, k)) {
          it = FTIndexIterator.union(iter(pointer(p, s), size(p, s), inZ, token), it);
        }
        p += s + ENTRY;
      }
    }
    return it;
  }

  /**
   * Performs a wildcard search for the specified token.
   * @param token token to look for
   * @return iterator
   */
  private IndexIterator wc(final byte[] token) {
    final FTIndexIterator it = FTIndexIterator.FTEMPTY;
    final FTWildcard wc = new FTWildcard(token);
    if(!wc.parse()) return it;

    final IntList pr = new IntList();
    final IntList ps = new IntList();
    final byte[] pref = wc.prefix();
    final int pl = pref.length, tl = tp.length;
    final int l = Math.min(tl - 1, wc.max());
    for(int ti = pl; ti <= l; ti++) {
      int i = tp[ti];
      if(i == -1) continue;
      int c = ti + 1;
      int e = -1;
      while(c < tl && e == -1) e = tp[c++];
      i = find(pref, i, e, ti);

      while(i < e) {
        final byte[] t = inY.readBytes(i, ti);
        if(!startsWith(t, pref)) break;
        if(wc.match(t)) {
          inZ.cursor(pointer(i, ti));
          final int s = size(i, ti);
          for(int d = 0; d < s; d++) {
            pr.add(inZ.readNum());
            ps.add(inZ.readNum());
          }
        }
        i += ti + ENTRY;
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
        return new TokenBuilder(token).add('(').addExt(size).add("x)").toString();
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
      final double[] v = new double[s];
      for(int i = 0; i < s; i++) v[i] = (long) pr.get(i) << 32 | ps.get(i);
      order = Array.createOrder(v, true);
      pre = pr;
      pos = ps;
    }
  }

  @Override
  public void add(final ValueCache vc) {
    throw Util.notExpected();
  }

  @Override
  public void delete(final ValueCache vc) {
    throw Util.notExpected();
  }

  @Override
  public void flush() { }
}

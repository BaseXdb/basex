package org.basex.index.ft;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;

import java.io.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.io.random.*;
import org.basex.query.ft.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * <p>This class provides access to a fuzzy full-text index structure
 * stored on disk. Each token has an entry in sizes, saving its length and a
 * pointer on ftdata, where to find the token and its ftdata.
 * The three database index files start with the prefix
 * {@link DataText#DATAFTX} and have the following format:</p>
 *
 * <ul>
 * <li>File <b>x</b> contains an entry for each token length:<br/>
 * Structure: {@code [l, p] ...}<br/>
 * {@code l} is the length of a token [byte].<br/>
 * {@code p} is the pointer of the first token with length {@code l} [int].
 * </li>
 * <li>File <b>y</b> contains the tokens and references:<br/>
 * Structure: {@code [t0, t1, ... tl, z, s]}<br/>
 * {@code t0, t1, ... tl-1} is the token [byte[l]]<br/>
 * {@code z} is the pointer on the data entries of the token [long]<br/>
 * {@code s} is the number of pre values, saved in data [int]
 * </li>
 * <li>File <b>z</b> contains the {@code pre/pos} references.
 *   The values are ordered, but not distinct:<br/>
 *   {@code pre1/pos1, pre2/pos2, pre3/pos3, ...} [{@link Num}]</li>
 * </ul>
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FTIndex implements Index {
  /** Entry size. */
  private static final int ENTRY = 9;

  /** Cached texts. Increases used memory, but speeds up repeated queries. */
  private final IntMap<byte[]> ctext = new IntMap<byte[]>();
  /** Levenshtein reference. */
  private final Levenshtein ls = new Levenshtein();
  /** Data reference. */
  private final Data data;

  /** Index storing each unique token length and pointer
   * on the first token with this length. */
  private final DataAccess inX;
  /** Index storing each token, its data size and pointer on the data. */
  final DataAccess inY;
  /** Storing pre and pos values for each token. */
  final DataAccess inZ;

  /** Cache for number of hits and data reference per token. */
  final IndexCache cache = new IndexCache();
  /** Scoring mode. 1 = document based, 2 = text-node based .*/
  final int scm;
  /** Minimum scoring value. */
  final double max;
  /** Minimum scoring value. */
  final double min;
  /** Token positions. */
  final int[] tp;

  /**
   * Constructor, initializing the index structure.
   * @param d data reference
   * @throws IOException I/O Exception
   */
  public FTIndex(final Data d) throws IOException {
    data = d;
    scm = d.meta.scoring;
    max = Math.log(data.meta.maxscore + 1);
    min = Math.log(data.meta.minscore - 1);

    // cache token length index
    inY = new DataAccess(d.meta.dbfile(DATAFTX + 'y'));
    inZ = new DataAccess(d.meta.dbfile(DATAFTX + 'z'));
    inX = new DataAccess(d.meta.dbfile(DATAFTX + 'x'));
    tp = new int[d.meta.maxlen + 3];
    for(int i = 0; i < tp.length; ++i) tp[i] = -1;
    int is = inX.readNum();
    while(--is >= 0) {
      int p = inX.readNum();
      final int r;
      if(p < tp.length) {
        r = inX.read4();
      } else {
        // legacy issue (7.0.2 -> 7.1)
        r = p << 24 | (inX.read1() & 0xFF) << 16 |
            (inX.read1() & 0xFF) << 8 | inX.read1() & 0xFF;
        p = p >> 8 | 0x40;
      }
      tp[p] = r;
    }
    tp[tp.length - 1] = (int) inY.length();
  }

  @Override
  public synchronized int count(final IndexToken it) {
    final byte[] key = it.get();
    if(key.length > data.meta.maxlen) return Integer.MAX_VALUE;

    // estimate costs for queries which stretch over multiple index entries
    final FTOpt opt = ((FTLexer) it).ftOpt();
    if(opt.is(FZ) || opt.is(WC)) return Math.max(1, data.meta.size / 10);

    return entry(key).size;
  }

  @Override
  public synchronized IndexIterator iter(final IndexToken it) {
    final byte[] tok = it.get();

    // wildcard search
    final FTOpt opt = ((FTLexer) it).ftOpt();
    if(opt.is(WC)) return wc(tok);

    // fuzzy search
    if(opt.is(FZ)) {
      final int k = data.meta.prop.num(Prop.LSERROR);
      return fuzzy(tok, k == 0 ? tok.length >> 2 : k);
    }

    // return cached or new result
    final IndexEntry e = entry(tok);
    return e.size > 0 ? iter(e.pointer, e.size, inZ) : FTIndexIterator.FTEMPTY;
  }

  /**
   * Returns a cache entry.
   * @param tok token to be found or cached
   * @return cache entry
   */
  private IndexEntry entry(final byte[] tok) {
    final IndexEntry e = cache.get(tok);
    if(e != null) return e;

    final long p = token(tok);
    return p == -1 ? new IndexEntry(tok, 0, 0) :
      cache.add(tok, size(p, tok.length), pointer(p, tok.length));
  }

  @Override
  public EntryIterator entries(final byte[] prefix) {
    return new EntryIterator() {
      int ti = prefix.length - 1, i, e, nr;
      boolean inner;

      @Override
      public synchronized byte[] next() {
        if(inner) {
          // loop through all entries with the same character length
          if(i < e) {
            final byte[] entry = inY.readBytes(i, ti);
            if(startsWith(entry, prefix)) {
              final long poi = inY.read5();
              nr = inY.read4();
              if(prefix.length != 0) cache.add(entry, nr, poi);
              i += ti + ENTRY;
              return entry;
            }
          }
        }
        // find next available entry group
        while(++ti < tp.length - 1) {
          i = tp[ti];
          if(i != -1) {
            int c = ti + 1;
            do e = tp[c++]; while(e == -1);
            nr = 0;
            inner = true;
            i = find(prefix, i, e, ti);
            // jump to inner loop
            final byte[] n = next();
            if(n != null) return n;
          }
        }
        // all entries processed: return null
        return null;
      }
      @Override
      public int count() {
        return nr;
      }
    };
  }

  /**
   * Binary search.
   * @param key key to be found
   * @param i start position
   * @param e end position
   * @param ti entry length
   * @return position where the key was found, or would have been found
   */
  int find(final byte[] key, final int i, final int e, final int ti) {
    final int tl = ti + ENTRY;
    int l = 0, h = (e - i) / tl;
    while(l <= h) {
      final int m = l + h >>> 1;
      final int p = i + m * tl;
      byte[] txt = ctext.get(p);
      if(txt == null) {
        txt = inY.readBytes(p, ti);
        ctext.add(p, txt);
      }
      final int d = diff(txt, key);
      if(d == 0) return i + m + tl;
      if(d < 0) l = m + 1;
      else h = m - 1;
    }
    return i + l * tl;
  }

  @Override
  public synchronized byte[] info() {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(LI_STRUCTURE + FUZZY + NL);
    tb.addExt("- %: %" + NL, STEMMING, Util.flag(data.meta.stemming));
    tb.addExt("- %: %" + NL, CASE_SENSITIVITY, Util.flag(data.meta.casesens));
    tb.addExt("- %: %" + NL, DIACRITICS, Util.flag(data.meta.diacritics));
    if(data.meta.language != null)
      tb.addExt("- %: %" + NL, LANGUAGE, data.meta.language);
    final long l = inX.length() + inY.length() + inZ.length();
    tb.add(LI_SIZE + Performance.format(l, true) + NL);

    final IndexStats stats = new IndexStats(data);
    addOccs(stats);
    stats.print(tb);
    return tb.finish();
  }

  @Override
  public synchronized void close() {
    inX.close();
    inY.close();
    inZ.close();
  }

  /**
   * Determines the pointer on a token.
   * @param tok token looking for
   * @return int pointer or {@code -1} if token was not found
   */
  private int token(final byte[] tok) {
    final int tl = tok.length;
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
      final int c = diff(inY.readBytes(m, tl), tok);
      if(c == 0) return m;
      if(c < 0) l = m + o;
      else r = m - o;
    }
    // accept entry if pointer is inside relevant tokens
    return r != x && l == r && eq(inY.readBytes(l, tl), tok) ? l : -1;
  }

  /**
   * Collects all tokens and their sizes found in the index structure.
   * @param stats statistics
   */
  private void addOccs(final IndexStats stats) {
    int i = 0;
    while(i < tp.length && tp[i] == -1) ++i;
    int p = tp[i];
    int j = i + 1;
    while(j < tp.length && tp[j] == -1) ++j;

    while(p < tp[tp.length - 1]) {
      if(stats.adding(size(p, i))) stats.add(inY.readBytes(p, i));
      p += i + ENTRY;
      if(p == tp[j]) {
        i = j;
        while(j + 1 < tp.length && tp[++j] == -1);
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
   * @param tok token to look for
   * @param k number of errors allowed
   * @return iterator
   */
  private IndexIterator fuzzy(final byte[] tok, final int k) {
    FTIndexIterator it = FTIndexIterator.FTEMPTY;
    final int tl = tok.length;
    final int e = Math.min(tp.length - 1, tl + k);
    int s = Math.max(1, tl - k) - 1;

    while(++s <= e) {
      int p = tp[s];
      if(p == -1) continue;
      int i = s + 1;
      int r = -1;
      while(i < tp.length && r == -1) r = tp[i++];
      while(p < r) {
        if(ls.similar(inY.readBytes(p, s), tok, k)) {
          it = FTIndexIterator.union(iter(pointer(p, s), size(p, s), inZ), it);
        }
        p += s + ENTRY;
      }
    }
    return it;
  }

  /**
   * Performs a wildcard search for the specified token.
   * @param tok token to look for
   * @return iterator
   */
  private IndexIterator wc(final byte[] tok) {
    FTIndexIterator it = FTIndexIterator.FTEMPTY;
    final FTWildcard wc = new FTWildcard();
    if(!wc.parse(tok)) return it;
    final int e = Math.min(tp.length - 1, wc.max());
    for(int s = wc.min(); s <= e; s++) {
      int p = tp[s];
      if(p == -1) continue;
      int i = s + 1;
      int r = -1;
      while(i < tp.length && r == -1) r = tp[i++];
      while(p < r) {
        if(wc.match(inY.readBytes(p, s))) {
          it = FTIndexIterator.union(iter(pointer(p, s), size(p, s), inZ), it);
        }
        p += s + ENTRY;
      }
    }
    return it;
  }

  /**
   * Returns an iterator for an index entry.
   * @param off offset on entries
   * @param size number of pre/pos entries
   * @param da data source
   * @return iterator
   */
  private synchronized FTIndexIterator iter(final long off, final int size,
      final DataAccess da) {

    // cache results
    da.cursor(off);
    final IntList vals = new IntList();
    for(int c = 0, lp = 0; c < size;) {
      if(lp == 0) {
        if(scm > 0) vals.add(da.readNum());
        lp = da.readNum();
        vals.add(lp);
      }
      final int pr = lp;
      vals.add(da.readNum());
      while(++c < size) {
        lp = da.readNum();
        vals.add(lp);
        if(lp != pr) break;
        vals.add(da.readNum());
      }
    }

    return new FTIndexIterator() {
      final FTMatches all = new FTMatches(toknum);
      int c, pre, lpre;
      double sc = -1;

      @Override
      public synchronized boolean more() {
        if(c == vals.size()) return false;
        if(lpre == 0) {
          if(scm > 0) sc = (Math.log(vals.get(c++)) - min) / (max - min);
          lpre = vals.get(c++);
        }
        pre = lpre;

        all.reset(toknum);
        all.or(vals.get(c++));

        while(c < vals.size() && (lpre = vals.get(c++)) == pre) {
          all.or(vals.get(c++));
        }
        return true;
      }

      @Override
      public synchronized FTMatches matches() {
        return all;
      }

      @Override
      public synchronized int next() {
        return pre;
      }

      @Override
      public synchronized int size() {
        return size;
      }

      @Override
      public synchronized double score() {
        return sc;
      }

      @Override
      public String toString() {
        return Integer.toString(size);
      }
    };
  }
}

package org.basex.index.ft;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.DataText;
import org.basex.index.IndexIterator;
import org.basex.index.IndexStats;
import org.basex.index.IndexToken;
import org.basex.io.random.DataAccess;
import org.basex.util.Levenshtein;
import org.basex.util.Num;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;
import org.basex.util.ft.FTLexer;
import org.basex.util.hash.TokenIntMap;

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
 * @author Sebastian Gath
 */
final class FTFuzzy extends FTIndex {
  /** Entry size. */
  private static final int ENTRY = 9;
  /** Token positions. */
  private final int[] tp;
  /** Levenshtein reference. */
  private final Levenshtein ls = new Levenshtein();

  /** Index storing each unique token length and pointer
   * on the first token with this length. */
  private final DataAccess inX;
  /** Index storing each token, its data size and pointer on the data. */
  private final DataAccess inY;
  /** Storing pre and pos values for each token. */
  private final DataAccess inZ;

  /**
   * Constructor, initializing the index structure.
   * @param d data reference
   * @throws IOException I/O Exception
   */
  protected FTFuzzy(final Data d) throws IOException {
    super(d);

    // cache token length index
    inY = new DataAccess(d.meta.dbfile(DATAFTX + 'y'));
    inZ = new DataAccess(d.meta.dbfile(DATAFTX + 'z'));
    inX = new DataAccess(d.meta.dbfile(DATAFTX + 'x'));
    tp = new int[d.meta.maxlen + 3];
    for(int i = 0; i < tp.length; ++i) tp[i] = -1;
    int is = inX.readNum();
    while(--is >= 0) {
      final int p = inX.readNum();
      tp[p] = inX.read4();
    }
    tp[tp.length - 1] = (int) inY.length();
  }

  @Override
  public synchronized int count(final IndexToken ind) {
    if(ind.get().length > data.meta.maxlen) return Integer.MAX_VALUE;

    // estimate costs for queries which stretch over multiple index entries
    final FTLexer lex = (FTLexer) ind;
    if(lex.ftOpt().is(FZ)) return Math.max(1, data.meta.size / 10);

    final byte[] tok = lex.get();
    final int id = cache.id(tok);
    if(id > 0) return cache.size(id);

    int s = 0;
    long poi = 0;
    final long p = token(tok);
    if(p > -1) {
      s = size(p, tok.length);
      poi = pointer(p, tok.length);
    }
    cache.add(tok, s, poi);
    return s;
  }

  @Override
  public synchronized IndexIterator iter(final IndexToken ind) {
    final byte[] tok = ind.get();

    // support fuzzy search
    if(((FTLexer) ind).ftOpt().is(FZ)) {
      int k = data.meta.prop.num(Prop.LSERROR);
      if(k == 0) k = tok.length >> 2;
      return fuzzy(tok, k, false);
    }

    // return cached or new result
    final int id = cache.id(tok);
    if(id == 0) {
      final int p = token(tok);
      return p > -1 ? iter(pointer(p, tok.length),
          size(p, tok.length), inZ, false) : FTIndexIterator.FTEMPTY;
    }
    return iter(cache.pointer(id), cache.size(id), inZ, false);
  }

  @Override
  public TokenIntMap entries(final byte[] prefix) {
    final TokenIntMap tim = new TokenIntMap();

    for(int s = prefix.length; s < tp.length - 1; s++) {
      int p = tp[s];
      if(p == -1) continue;
      int i = s + 1;
      int r = -1;
      do r = tp[i++]; while(r == -1);
      inY.cursor(p);
      boolean f = false;
      while(p < r) {
        final byte[] tok = inY.readBytes(s);
        final long poi = inY.read5();
        final int size = inY.read4();
        cache.add(tok, size, poi);
        if(startsWith(tok, prefix)) {
          tim.add(tok, size);
          f = true;
        } else if(f) {
          break;
        }
        p += s + ENTRY;
      }
    }
    return tim;
  }

  @Override
  public synchronized byte[] info() {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(INDEXSTRUC + FUZZYSTRUC + NL);
    tb.addExt("- %: %" + NL, CREATEST, Util.flag(data.meta.stemming));
    tb.addExt("- %: %" + NL, CREATECS, Util.flag(data.meta.casesens));
    tb.addExt("- %: %" + NL, CREATEDC, Util.flag(data.meta.diacritics));
    if(data.meta.language != null)
      tb.addExt("- %: %" + NL, CREATELN, data.meta.language);
    final long l = inX.length() + inY.length() + inZ.length();
    tb.add(SIZEDISK + Performance.format(l, true) + NL);

    final IndexStats stats = new IndexStats(data);
    addOccs(stats);
    stats.print(tb);
    return tb.finish();
  }

  @Override
  public synchronized void close() throws IOException {
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
   * Performs a fuzzy search for token, with e maximal number
   * of errors e.
   * @param tok token looking for
   * @param k number of errors allowed
   * @param f fast evaluation
   * @return iterator
   */
  private IndexIterator fuzzy(final byte[] tok, final int k, final boolean f) {
    FTIndexIterator it = FTIndexIterator.FTEMPTY;
    final int tl = tok.length;
    final int e = Math.min(tp.length, tl + k);
    int s = Math.max(1, tl - k) - 1;

    final int err = data.meta.prop.num(Prop.LSERROR);
    while(++s <= e) {
      int p = tp[s];
      if(p == -1) continue;
      int i = s + 1;
      int r = -1;
      do r = tp[i++]; while(r == -1);
      while(p < r) {
        if(ls.similar(inY.readBytes(p, s), tok, err)) {
          it = FTIndexIterator.union(
              iter(pointer(p, s), size(p, s), inZ, f), it);
        }
        p += s + ENTRY;
      }
    }
    return it;
  }
}

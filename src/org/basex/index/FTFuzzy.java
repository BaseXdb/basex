package org.basex.index;

import static org.basex.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.io.DataAccess;
import org.basex.util.Levenshtein;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;
import org.basex.util.Tokenizer;

/**
 * This class provides access to attribute values and text contents
 * stored on disk. The data is stored in two files, ftdata and sizes.
 * Sizes provides a kind of table of contents, using the first char
 * of a token and a pointer on the data entry. Normally there are chars from
 * a-z and 0-9 and the corresponding pointers on the first entry starting with
 * this char.
 * Each token has an entry in sizes, saving its length and a pointer on ftdata,
 * where to find the token and its ftdata.
 *
 * The structure of li:
 *    [l, p] ... where l is the length of a token an p the pointer of
 *                the first token with length l; there's an entry for
 *                each token length [byte, int]
 *
 * The structure of lt:
 *    [t0, t1, ... tl, z, s] ... where t0, t1, ... tl are the byte values
 *                           of the token (byte[l]); z is the pointer on
 *                           the data entries of the token (int) and s is
 *                           the number of pre values, saved in data (int)
 *
 *
 * The structure of dat:
 *    [pre0, ..., pres, pos0, pos1, ..., poss] where pre and pos are the
 *                          ft data [int[]]
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Sebastian Gath
 */
public final class FTFuzzy extends FTIndex {
  /** Entry size. */
  private static final int ENTRY = 9;
  /** Levenshtein reference. */
  private final Levenshtein ls = new Levenshtein();

  /** Index storing each unique token length and pointer
   * on the first token with this length. */
  private final DataAccess li;
  /** Index storing each token, its data size and pointer
   * on then data. */
  private final DataAccess ti;
  /** Storing pre and pos values for each token. */
  private final DataAccess dat;
  /** Token positions. */
  private final int[] tp = new int[MAXLEN + 3];

  /**
   * Constructor, initializing the index structure.
   * @param d data reference
   * @throws IOException IO Exception
   */
  public FTFuzzy(final Data d) throws IOException {
    super(d);
    final String db = d.meta.name;
    ti = new DataAccess(db, DATAFTX + 'y', d.meta.prop);
    dat = new DataAccess(db, DATAFTX + 'z', d.meta.prop);

    // cache token length index
    li = new DataAccess(db, DATAFTX + 'x', d.meta.prop);
    for(int i = 0; i < tp.length; i++) tp[i] = -1;
    int is = li.read1();
    while(--is >= 0) {
      final int p = li.read1();
      tp[p] = li.read4();
    }
    tp[tp.length - 1] = (int) ti.length();
  }

  @Override
  public byte[] info() {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(FUZZY + NL);
    tb.add("- %: %\n", CREATESTEM, BaseX.flag(data.meta.ftst));
    tb.add("- %: %\n", CREATECS, BaseX.flag(data.meta.ftcs));
    tb.add("- %: %\n", CREATEDC, BaseX.flag(data.meta.ftdc));
    final long l = li.length() + ti.length() + dat.length();
    tb.add(SIZEDISK + Performance.format(l, true) + NL);

    final IndexStats stats = new IndexStats(data.meta.prop);
    addOccs(stats);
    stats.print(tb);
    return tb.finish();
  }

  @Override
  public int nrIDs(final IndexToken ind) {
    // skip result count for queries which stretch over multiple index entries
    final Tokenizer fto = (Tokenizer) ind;
    if(fto.fz || fto.wc) return 1;

    final byte[] tok = fto.get();
    final int id = cache.id(tok);
    if(id > 0) return cache.getSize(id);

    int size = 0;
    long poi = 0;
    final long p = token(tok);
    if(p > -1) {
      size = size(p, tok.length);
      poi = pointer(p, tok.length);
    }
    cache.add(tok, size, poi);
    return size;
  }

  @Override
  public IndexIterator ids(final IndexToken ind) {
    final Tokenizer ft = (Tokenizer) ind;
    final byte[] tok = ft.get();

    // support fuzzy search
    if(ft.fz) {
      int k = data.meta.prop.num(Prop.LSERR);
      if(k == 0) k = tok.length >> 2;
      return fuzzy(tok, k, ft.fast);
    }

    // return cached or new result
    final int id = cache.id(tok);
    return id == 0 ? get(tok, ft.fast) :
      iter(cache.getPointer(id), cache.getSize(id), dat, ft.fast);
  }

  @Override
  public synchronized void close() throws IOException {
    li.close();
    ti.close();
    dat.close();
  }

  /**
   * Determines the pointer on a token.
   * @param tok token looking for.
   * @return int pointer or -1 if token was not found
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
      final int m = l + (r - l) / 2 / o * o;
      final int c = diff(ti.readBytes(m, m + tl), tok);
      if(c == 0) return m;
      if(c < 0) l = m + o;
      else r = m - o;
    }
    // accept entry if pointer is inside relevant tokens
    return r != x && l == r && eq(ti.readBytes(l, l + tl), tok) ? l : -1;
  }

  /**
   * Collects all tokens and their sizes found in the index structure.
   * @param stats statistic reference
   */
  private void addOccs(final IndexStats stats) {
    int i = 0;
    while(i < tp.length && tp[i] == -1) i++;
    int p = tp[i];
    int j = i + 1;
    while(j < tp.length && tp[j] == -1) j++;

    while(p < tp[tp.length - 1]) {
      if(stats.adding(size(p, i))) stats.add(ti.readBytes(p, p + i));
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
    return ti.read5(pt + lt * 1L);
  }

  /**
   * Reads the size of ftdata from disk.
   * @param pt pointer on token
   * @param lt length of the token
   * @return size of the ftdata
   */
  private int size(final long pt, final int lt) {
    return ti.read4(pt + lt + 5);
  }

  /**
   * Performs a fuzzy search for token, with e maximal number
   * of errors e.
   *
   * @param tok token looking for
   * @param k number of errors allowed
   * @param f fast evaluation
   * @return int[][] data
   */
  private IndexIterator fuzzy(final byte[] tok, final int k, final boolean f) {
    FTIndexIterator it = FTIndexIterator.EMP;

    final int tl = tok.length;
    final int e = Math.min(tp.length, tl + k);
    int s = Math.max(1, tl - k) - 1;

    int err = data.meta.prop.num(Prop.LSERR);
    while(++s <= e) {
      int p = tp[s];
      if(p == -1) continue;
      int i = s + 1;
      int r = -1;
      do r = tp[i++]; while(r == -1);
      while(p < r) {
        if(ls.similar(ti.readBytes(p, p + s), tok, err)) {
          it = FTIndexIterator.union(
              iter(pointer(p, s), size(p, s), dat, f), it);
        }
        p += s + ENTRY;
      }
    }
    return it;
  }

  /**
   * Get pre- and pos values, stored for token out of index.
   * @param tok token looking for
   * @param f fast evaluation
   * @return iterator
   */
  private FTIndexIterator get(final byte[] tok, final boolean f) {
    final int p = token(tok);
    return p > -1 ? iter(pointer(p, tok.length),
        size(p, tok.length), dat, f) : FTIndexIterator.EMP;
  }
}

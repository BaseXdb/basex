package org.basex.index;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.core.proc.AInfo;
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
  protected FTFuzzy(final Data d) throws IOException {
    super(d);

    // cache token length index
    ti = new DataAccess(d.meta.file(DATAFTX + 'y'));
    dat = new DataAccess(d.meta.file(DATAFTX + 'z'));
    li = new DataAccess(d.meta.file(DATAFTX + 'x'));
    for(int i = 0; i < tp.length; i++) tp[i] = -1;
    int is = li.read1();
    while(--is >= 0) {
      final int p = li.read1();
      tp[p] = li.read4();
    }
    tp[tp.length - 1] = (int) ti.length();
  }

  /**
   * Constructor, initializing the index structure.
   * @param d data reference
   * @param cf current file
   * @throws IOException IO Exception
   */
  public FTFuzzy(final Data d, final byte cf) throws IOException {
    super(d);

    // cache token length index
    ti = new DataAccess(d.meta.file(DATAFTX + cf + 'y'));
    dat = new DataAccess(d.meta.file(DATAFTX + cf + 'z'));
    li = new DataAccess(d.meta.file(DATAFTX + cf + 'x'));
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
    tb.add("- %: %" + NL, CREATESTEM, AInfo.flag(data.meta.stemming));
    tb.add("- %: %" + NL, CREATECS, AInfo.flag(data.meta.casesens));
    tb.add("- %: %" + NL, CREATEDC, AInfo.flag(data.meta.diacritics));
    final long l = li.length() + ti.length() + dat.length();
    tb.add(SIZEDISK + Performance.format(l, true) + NL);

    final IndexStats stats = new IndexStats(data);
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
      int k = data.meta.prop.num(Prop.LSERROR);
      if(k == 0) k = tok.length >> 2;
      return fuzzy(tok, k, ft.fast);
    }

    // return cached or new result
    final int id = cache.id(tok);
    return id == 0 ? get(tok, ft.fast) :
      iter(cache.getPointer(id), cache.getSize(id), dat, ft.fast);
  }

  @Override
  public void close() throws IOException {
    li.close();
    ti.close();
    dat.close();
  }
  /** Pointer on current token length. */
  int ctl = 0;
  /** Pointer on next token length. */
  int ntl = 0;
  /** Number of written bytes for tokens. */
  int ptok = 0;
  /** Pointer on full-text data. */
  long pftd;
  /** Next number of pre values. */
  int fts;
  
  /**
   * Returns next Token.
   * @return byte[] token
   */
  public byte[] nextTok() {
    if(tp[tp.length - 1] == ptok) return new byte[]{};   
    if (tp[ntl] == ptok || ntl == 0) {
      ctl++;
      while (tp[ctl] == -1) ctl++;
      ntl = ctl + 1;
      while (tp[ntl] == -1) ntl++;
    }
        
    if (ctl == tp.length) return new byte[]{};
    final byte[] tok = ti.readBytes(ptok, ptok + ctl);
    pftd = ti.read5(ti.pos());
    fts = ti.read4();
    ptok = (int) ti.pos();
//    if (tp[ctl + 1] == ptok) ctl++; 
    return tok;
  }
  
  /**
   * Returns next number of pre-values.
   * @return int number of pre-values
   */
  public int nextFTDataSize() {
    return fts;
  }
  /** Next pre values. */   
  int[] prv;
  /** Next pos values. */
  int[] pov;
  /**
   * Returns next pre values.
   * @return int[] pre values
   */
  public int[] nextPreValues() {
//    dat.cursor(pftd);
    prv = new int[fts];
    pov = new int[fts];
    for (int j = 0; j < fts; j++) {
      prv[j] = dat.readNum();
      pov[j] = dat.readNum();
    }
    return prv;
  }

  /**
   * Returns next pos values.
   * @return int[] pos values
   */
  public int[] nextPosValues() {
    return pov;
  }

  
  /**
   * Determines the pointer on a token.
   * @param tok token looking for
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

    final int err = data.meta.prop.num(Prop.LSERROR);
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
   * Gets pre- and pos values, stored for token out of index.
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

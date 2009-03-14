package org.basex.index;

import static org.basex.Text.*;
import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.io.DataAccess;
import org.basex.util.IntList;
import org.basex.util.Levenshtein;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;

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
public final class FTFuzzy extends Index {
  /** Levenshtein reference. */
  private final Levenshtein ls = new Levenshtein();
  /** Cache for number of hits and data reference per token. */
  private final FTTokenMap cache = new FTTokenMap();

  /** Values file. */
  private final Data data;
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
   * @param db name of the database
   * @throws IOException IO Exception
   */
  public FTFuzzy(final Data d, final String db) throws IOException {
    ti = new DataAccess(db, DATAFTX + "y");
    dat = new DataAccess(db, DATAFTX + "z");
    data = d;

    // cache token length index
    li = new DataAccess(db, DATAFTX + "x");
    for(int i = 0; i < tp.length; i++) tp[i] = -1;
    int is = li.read();
    while(--is >= 0) {
      final int p = li.read();
      tp[p] = li.readInt();
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

    final IndexStats stats = new IndexStats();
    addOccs(stats);
    stats.print(tb);
    return tb.finish();
  }

  @Override
  public int nrIDs(final IndexToken index) {
    // skip count of queries which stretch over multiple index entries
    final FTTokenizer fto = (FTTokenizer) index;
    if(fto.fz || fto.wc) return 1;

    final byte[] tok = index.get();
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
    final FTTokenizer ft = (FTTokenizer) ind;
    final byte[] tok = ft.get();

    // support fuzzy search
    if(ft.fz) {
      int k = Prop.lserr;
      if(k == 0) k = Math.max(1, tok.length >> 2);
      return fuzzy(tok, k);
    }

    // return cached or new result
    final int id = cache.id(tok);
    return id == 0 ? get(tok) :
      data(cache.getPointer(id), cache.getSize(id), dat, data);
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
   * @return int pointer
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
    int x = r;

    // binary search
    final int o = tl + 9;
    while(l < r) {
      final int m = l + (r - l) / 2 / o * o;
      final int c = diff(ti.readBytes(m, m + tl), tok);
      if(c == 0) return m;
      else if(c < 0) l = m + o;
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
      p += i + 9;
      if (p == tp[j]) {
        i = j;
        while(j + 1 < tp.length && tp[++j] == -1);
      }
    }
  }

  /**
   * Get the pointer on ftdata for a token.
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
    return ti.readInt(pt + lt + 5);
  }

  /**
   * Extracts data from disk and returns it in
   * [[pre1, ..., pres], [pos1, ..., poss]] representation.
   *
   * @param s number of pre/pos values
   * @param p pointer on data
   * @param da DataAccess for reading ftdata
   * @param d data reference
   * @return int[][] data
   */
  static IndexArrayIterator data(final long p, final int s,
      final DataAccess da, final Data d) {
    if(s == 0 || p < 0) return IndexArrayIterator.EMP;

    if(!d.meta.ftittr) {
      final int[][] dt = new int[2][s];
      da.cursor(p);
      for(int i = 0; i < s; i++) dt[0][i] = da.readNum();
      for(int i = 0; i < s; i++) dt[1][i] = da.readNum();
      return new IndexArrayIterator(dt, true);
    }

    return new IndexArrayIterator(s) {
      boolean f = true;
      int lpre = -1;
      int c = 0;
      long pos = p;
      FTNode n = new FTNode();

      @Override
      public boolean more() {
        if (c == s) return false;
        IntList il = new IntList();
        int pre;
        if (f) {
          f = false;
          pre = da.readNum(pos);
          pos = da.pos();
        } else {
          pre = lpre;
        }

        f = false;
        il.add(pre);
        il.add(da.readNum(pos));
        while(++c < s && (lpre = da.readNum()) == pre) il.add(da.readNum());
        pos = da.pos();
        n = new FTNode(il.finish(), 1);
        return true;
      }

      @Override
      public FTNode nextFTNode() {
        n.genPointer(toknum);
        if(tok != null) n.setToken(tok);
        return n;
      }

      @Override
      public int next() {
        return n.getPre();
      }
    };
  }

  /**
   * Performs a fuzzy search for token, with e maximal number
   * of errors e.
   *
   * @param tok token looking for
   * @param k number of errors allowed
   * @return int[][] data
   */
  private IndexIterator fuzzy(final byte[] tok, final int k) {
    IndexArrayIterator it = new IndexArrayIterator(0);

    final int tl = tok.length;
    final int e = Math.min(tp.length, tl + k);
    int s = Math.max(1, tl - k) - 1;

    while(++s <= e) {
      int p = tp[s];
      if(p == -1) continue;
      int i = s + 1;
      int r = -1;
      do r = tp[i++]; while(r == -1);
      while(p < r) {
        if (ls.similar(ti.readBytes(p, p + s), tok)) {
          it = IndexArrayIterator.merge(data(pointer(p, s),
              size(p, s), dat, data), it);
        }
        p += s + 9;
      }
    }
    return it;
  }

  /**
   * Get pre- and pos values, stored for token out of index.
   * @param tok token looking for
   * @return iterator
   */
  private IndexArrayIterator get(final byte[] tok) {
    int p = token(tok);
    return p > -1 ? data(pointer(p, tok.length),
        size(p, tok.length), dat, data) : IndexArrayIterator.EMP;
  }

  /*
   * Performs db-access and checks real case of a token.
   * @param ids full-text ids
   * @param d Data-reference
   * @param ftdb Fulltext Tokenizer
   * @param token token
   * @param pw position of wildcard
   * @return counter
  static IndexArrayIterator csDBCheck(final IndexArrayIterator ids,
      final Data d, final FTTokenizer ftdb, final byte[] token, final int pw) {

    return new IndexArrayIterator(1) {
      FTNode r;

      @Override
      public boolean more() {
        r = new FTNode();
        while (ids.more()) {
          r = ids.nextFTNode();
          ftdb.init(d.text(r.getPre()));
          int i = 0;
          ftdb.more();
          // iterator text values
          while(r.morePos()) {
            ftdb.cs = false;
            while (i < r.nextPos() && ftdb.more()) i++;

            // token found - match case sensitivity
            ftdb.cs = true;
            //if (pw < token.length) {
              final byte[] b = ftdb.get();
              if (pw > b.length  || !eq(substring(token, 0, pw),
                  substring(b, 0, pw))) {
                r.removePos();
              }
            //} else if(!eq(token, ftdb.get())) {
            //  r.removePos();
            //}
          }
          if (r.hasPos()) return true;
        }
        return false;
      }

      @Override
      public FTNode nextFTNode() {
        r.genPointer(toknum);
        r.reset();
        if (tok != null) r.setToken(tok);
        return r;
      }

      @Override
      public int next() {
        return r.getPre();
      }
    };
  }

  /**
   * Performs a wildcard search. Only one '.*' is supported.
   *
   * @param tok token containing a wildcard
   * @param posw position of the wildcard in tok
   * @return data found
  private IndexArrayIterator getTokenWildCard(final byte[] tok,
      final int posw) {
    if (tok[posw] == '.' && tok.length > posw + 1 && tok[posw + 1] == '*') {
      int[][] b;
      IndexArrayIterator it = new IndexArrayIterator(0);
      byte[] dtok;
      b = getBoundPointer(tok.length - 2);
      while (true) {
        if (b[0][1] >= b[1][1]) {
          // set new bounds
          b[0] = b[1];
          b[1] = getNextBoundPointer();
          if (b[1] == null) break; //return data;
        }
        dtok = ti.readBytes(b[0][1], b[0][0] + b[0][1]);
        if (contains(tok, posw, dtok)) {
          it = IndexArrayIterator.merge(
            getData(getPointerOnData(b[0][1], b[0][0]),
                getDataSize(b[0][1], b[0][0]), dat, data), it);
        }
        b[0][1] += b[0][0] * 1L + 9L;
      }
      dtok = ti.readBytes(b[0][1], b[0][0] + b[0][1]);
      if (contains(tok, posw, dtok)) {
        it = IndexArrayIterator.merge(
            getData(getPointerOnData(b[0][1], b[0][0]),
                getDataSize(b[0][1], b[0][0]), dat, data), it);
      }
      return it;
    }

    // FuzzyIndex supports only '.*' as wildcard...
    BaseX.notexpected();
    return null;
  }

  /*
   * Compares two character arrays for equality and
   * checks if a is contained in b.
   * Tokens abc and abcde are calculated as equal!
   *
   * @param tokww token with the wildcard
   * @param posw position of the wildcard in tokww
   * @param tok2 second token to be compared
   * @return true if tok2 is contained in tokww
  private static boolean contains(final byte[] tokww, final int posw,
      final byte[] tok2) {
    if (tokww.length - 2 > tok2.length) return false;

    // check token before wildcard
    for(int t = 0; t < posw; t++) {
      if (tokww[t] != tok2[t]) return false;
    }

    // check token after wildcard
    for(int t = tokww.length - posw - 2; t > 0; t--) {
      if(tokww[tokww.length - t] != tok2[tok2.length - t]) return false;
    }

    return true;
  }

  /* Saves the last pointer on the index. Used for wildcard search.
  private int lastIndex = -1;

  /*
   * Returns the pointer on the first token with minimum length
   * tokl and the first pointer on the token with minimum length
   * tokl + 1.
   * @param tokl token length
   * @return int[][] pointer on token
  private int[][] getBoundPointer(final int tokl) {
    int i = 0;
    indexsize = li.readBytes(0, 1L)[0];
    int ts = li.readBytes(1L, 2L)[0];
    while(i < indexsize && tokl > ts) {
      i++;
      ts = li.readBytes(1L + i * 5L, 1L + i * 5L + 1L)[0];
    }

    if(i == indexsize) return null;

    // back up last pointer on index
    lastIndex = (int) (1L + (i + 1) * 5L);
    return new int[][] {
        // token length, pointer on the first token with this length
        { li.readBytes(1L + i * 5L, 1L + i * 5L + 1L)[0],
            li.readInt(1L + i * 5L + 1L) },
        { li.readBytes(1L + (i + 1) * 5L, 1L + (i + 1) * 5L + 1L)[0],
            li.readInt(1L + (i + 1) * 5L + 1L) } };
  }

  /*
   * Gets next pointer on a entry in length index.
   * @return entry from li
  private int[] getNextBoundPointer() {
    if ((lastIndex - 1L) / 5L + 1 == indexsize) return null;
    lastIndex += 5L;

    return new int[] {li.readBytes(lastIndex, lastIndex + 1L)[0],
        li.readInt(lastIndex + 1L)};
  }
   */
}

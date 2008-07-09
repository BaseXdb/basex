package org.basex.index;

import static org.basex.Text.*;
import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.io.DataAccessPerf;
import org.basex.query.xpath.expr.FTUnion;
import org.basex.util.Array;
import org.basex.util.FTTokenizer;
import org.basex.util.IntList;
import org.basex.util.Levenshtein;
import org.basex.util.Performance;
import org.basex.util.Token;
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
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class Fuzzy extends Index {
  /** Number of index entries. */
  int indexsize;
  /** Number of ftdata entries. */
  int ftdatasize;
  /** Values file. */
  final Data data;
  /** Index storing each unique token length and pointer
   * on the first token with this length. */
  final DataAccessPerf li;
  /** Index storing each token, its data size and pointer
   * on then data. */
  final DataAccessPerf ti;
  /** Storing pre and pos values for each token. */
  final DataAccessPerf dat;
  /** Token positions. */
  final int[] tp = new int[Token.MAXLEN + 1];

  /**
   * Constructor, initializing the index structure.
   * @param d data reference
   * @param db name of the database

   * @throws IOException IO Exception
   */
  public Fuzzy(final Data d, final String db) throws IOException {
    final String file = DATAFTX;
    ti = new DataAccessPerf(db, file + "y" , "Token");
    dat = new DataAccessPerf(db, file + "z", "FTData");
    data = d;

    // cache token length index
    li = new DataAccessPerf(db, file + "x", "TokenLengthIndex");
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
    tb.add("- %: %\n", CREATESTEM, BaseX.flag(data.meta.ftstem));
    tb.add("- %: %\n", CREATECS, BaseX.flag(data.meta.ftcs));
    tb.add("- %: %\n", CREATEDC, BaseX.flag(data.meta.ftdc));
    final long l = li.length() + ti.length() + dat.length();
    tb.add(SIZEDISK + Performance.formatSize(l, true) + NL);
    return tb.finish();
  }

  /**
   * Determines the pointer on a token.
   * @param tok token looking for.
   * @return int pointer
   */
  private int getPointerOnToken(final byte[] tok) {
    final int tl = tok.length;
    int l = tp[tl];
    if(l == -1) return -1;

    int r = -1;
    int i = 1;
    do r = tp[tl + i++]; while(r == -1);
    final int o = tl + 9;

    // binary search
    while(l < r) {
      final int m = l + (r - l) / 2 / o * o; // (r - l) / 2 - (r - l) / 2 % o
      final int c = Token.cmp(ti.readBytes(m, m + tl), tok);
      if(c == 0) return m;
      else if(c > 0) l = m + o;
      else r = m - o;
    }
    return r == l && Token.eq(ti.readBytes(l, l + tl), tok) ? l : -1;
  }

  /** Saves the last pointer on the index. Used for wildcardsearch. */
  private int lastIndex = -1;

  /**
   * Returns the pointer on the first token with minimum length
   * tokl and the first pointer on the token with minimum length
   * tokl + 1.
   *
   * @param tokl token length
   * @return int[][] pointer on token
   */
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

  /**
   * Gets next pointer on a entry in length index.
   * @return entry from li
   */
  private int[] getNextBoundPointer() {
    if ((lastIndex - 1L) / 5L + 1 == indexsize) return null;
    lastIndex += 5L;

    return new int[] {li.readBytes(lastIndex, lastIndex + 1L)[0],
        li.readInt(lastIndex + 1L)};
  }

  /**
   * Get the pointer on ftdata for a token.
   * @param pt pointer on token
   * @param lt length of the token
   * @return int pointer on ftdata
   */
  private long getPointerOnData(final long pt, final int lt) {
    return ti.read5(pt + lt * 1L);
  }

  /**
   * Reads the size of ftdata from disk.
   * @param pt pointer on token
   * @param lt length of the token
   * @return size of the ftdata
   */
  private int getDataSize(final long pt, final int lt) {
    return ti.readInt(pt + lt + 5);
  }

  /**
   * Reads the ftdata from disk.
   * @param p pointer of ftdata
   * @param s size of pre values
   * @return iterator
   */
  private IndexIterator getData(final long p, final int s) {
    final boolean cmp = data.meta.fcompress;
    final int[][] d = {
        cmp ? dat.readNums(p, s) : dat.readInts(p, p + s * 4L),
        cmp ? dat.readNums(s) : dat.readInts(p + s * 4L, p + 2 * s * 4L)
    };

    return new IndexIterator() {
      /** Counter. */
      private int c = -1;
      /** Pre value flag. */
      private boolean pre;

      @Override
      public boolean more() {
        return ++c < s;
      }
      @Override
      public int next() {
        return d[(pre ^= true) ? 0 : 1][c];
      }
      @Override
      public int size() {
        return s;
      }
    };
  }

  /*
  private IndexIterator getData(final long p, final int s) {
    dat.cursor(p);
    return new IndexIterator() {
      /* Counter.
      private int c = -1;
      @Override
      public boolean more() { return ++c < s; }
      @Override
      public int next() { return dat.readNum(); }
      @Override
      public int size() { return s; }
    };
  }
  */

  /**
   * Caches the iterator values and returns an int array (temporary).
   * @param it iterator
   * @return array
   */
  private int[][] finish(final IndexIterator it) {
    final IntList pre = new IntList();
    final IntList pos = new IntList();
    while(it.more()) {
      pre.add(it.next());
      pos.add(it.next());
    }
    return new int[][] { pre.finish(), pos.finish() };
  }

  /**
   * Performs a fuzzy search for token, with e maximal number
   * of errors e.
   *
   * @param tok token looking for
   * @param e number of errors allowed
   * @return int[][] data
   */
  private IndexIterator getFuzzy(final byte[] tok, final int e) {
    int[][] ft = null;
    byte[] to;

    int dif;

    int i = 0;
    final int is = li.readBytes(0, 1L)[0];
    int ts = li.readBytes(1L, 2L)[0];

    dif = (tok.length - ts < 0) ?
        ts - tok.length : tok.length - ts;
    while (i < is && dif > e) {
      i++;
      ts = li.readBytes(1L + i * 5L, 1L + i * 5L + 1L)[0];
      dif = (tok.length - ts < 0) ?
          ts - tok.length : tok.length - ts;
    }

    if (i == is) return null;

    int p;
    int pe;
    while (i < is && dif <= e) {
      p = li.readInt(1L + i * 5L + 1L);
      pe = li.readInt(1L + (i + 1) * 5L + 1L);

      while(p < pe) {
        to = ti.readBytes(p, p + ts);
        if (calcEQ(to, 0, tok, e)) {
          //System.out.println(new String(to));
          // read data
          ft = FTUnion.calculateFTOr(ft,
              finish(getData(getPointerOnData(p, ts), getDataSize(p, ts))));
        }
        p += ts + 4L  + 5L;
     }
      i++;
      ts = li.readBytes(1L + i * 5L, 1L + i * 5L + 1L)[0];
      dif = (tok.length - ts < 0) ?
          ts - tok.length : tok.length - ts;
    }
    return new IndexArrayIterator(ft[0], ft[1]);
  }

  /**
   * Get pre- and pos values, stored for token out of index.
   * @param tok token looking for
   * @return iterator
   */
  private IndexIterator get(final byte[] tok) {
    final long p = getPointerOnToken(tok);
    if (p == -1) return IndexIterator.EMPTY;
    return getData(getPointerOnData(p, tok.length), getDataSize(p, tok.length));
  }

  @Override
  public IndexIterator ids(final IndexToken ind) {
    final FTTokenizer ft = (FTTokenizer) ind;
    final byte[] tok = ft.get();

    if(ft.fz) {
      int k = Prop.lserr;
      if(k == 0) k = Math.max(1, tok.length >> 2);
      return getFuzzy(tok, k);
    }

    if(ft.wc) {
      final int pw = Token.indexOf(tok, '.');
      if(pw != -1) return getTokenWildCard(Token.lc(tok), pw);
    }

    // get result iterator
    final IndexIterator ii = get(Token.lc(tok));

    // case insensitive search..
    if(!ft.cs) return ii;

    // case sensitive search
    if(!ii.more()) return ii;

    // cache iterator results (temporary...)
    final int[][] ids = finish(ii);
    int c = 0;

    // check real case of each result node
    final FTTokenizer ftdb = new FTTokenizer();
    ftdb.st = ft.st;

    for(int i = 0; i < ids[0].length;) {
      final int id = ids[0][i];
      ftdb.init(data.text(id));

      // iterator text values
      while(id == ids[0][i] && ftdb.more()) {
        // first match case insensitive value
        ftdb.cs = false;
        if(!Token.eq(tok, ftdb.get())) continue;

        // token found - match case sensitivity
        ftdb.cs = true;
        if(Token.eq(tok, ftdb.get())) {
          // overwrite original values
          ids[0][c] = id;
          ids[1][c++] = ids[1][i];
        }
        i++;
      }
    }
    return new IndexArrayIterator(
        Array.finish(ids[0], c), Array.finish(ids[1], c));
  }

  @Override
  public int nrIDs(final IndexToken index) {
    // specified ft options are not checked yet...
    final byte[] tok = index.get();
    final long p = getPointerOnToken(Token.lc(tok));
    return p == -1 ? 0 : getDataSize(p, tok.length);
  }

  @Override
  public synchronized void close() throws IOException {
    li.close();
    ti.close();
    dat.close();
  }

  /**
   * Calculates the equality of tok1 and tok2, with respect to the
   * number or error e, using the Levenshtein algorithm.
   *
   * @param tok1 token1 to compare
   * @param sp start position in token
   * @param tok2 token2 to compare
   * @param e number of errors allowed
   * @return boolean as result
   */
  private static boolean calcEQ(final byte[] tok1, final int sp,
      final byte[] tok2, final int e) {
    final int df = Math.abs(tok1.length - tok2.length);
    if (df > e) return false;

    final int d = (tok1.length > tok2.length) ?
        Levenshtein.ls(tok1, sp, tok1.length, tok2, e) :
      Levenshtein.ls(tok2, sp, tok2.length, tok1, e);
    return d <= e;
  }

  /**
   * Performs a wildcard search. Only one '.*' is supported.
   *
   * @param tok token containing a wildcard
   * @param posw position of the wildcard in tok
   * @return data found
   */
  private IndexIterator getTokenWildCard(final byte[] tok, final int posw) {
    if (tok[posw] == '.' && tok.length > posw + 1 && tok[posw + 1] == '*') {
      int[][] b;
      int[][] dt = null;
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
        //System.out.println(new String(dtok));
        if (contains(tok, posw, dtok)) dt = FTUnion.calculateFTOr(dt,
            finish(getData(getPointerOnData(b[0][1], b[0][0]),
                getDataSize(b[0][1], b[0][0]))));
        // b[0][1] += b[0][0] * 1L + 8L;
        b[0][1] += b[0][0] * 1L + 9L;
        //}
      }
      dtok = ti.readBytes(b[0][1], b[0][0] + b[0][1]);
      if (contains(tok, posw, dtok)) dt = FTUnion.calculateFTOr(dt,
          finish(getData(getPointerOnData(b[0][1], b[0][0]),
              getDataSize(b[0][1], b[0][0]))));
      return new IndexArrayIterator(dt[0], dt[1]);
    }

    BaseX.debug("Sorry, FuzzyIndex supports only \'.*\' as wildcard.");
    return null;
  }

  /**
   * Compares two character arrays for equality and
   * checks if a is contained in b.
   * Tokens abc and abcde are calculated as equal!
   *
   * @param tokww token with the wildcard
   * @param posw position of the wildcard in tokww
   * @param tok2 second token to be compared
   * @return true if tok2 is contained in tokww
   */
  private static boolean contains(final byte[] tokww, final int posw,
      final byte[] tok2) {
    if (tokww.length - 2 > tok2.length) return false;

    // check token before wildcard
    for(int t = 0; t < posw; t++) {
      if (tokww[t] != tok2[t]) return false;
    }

    // check token after wildcard
    for(int t = tokww.length - posw - 2; t > 0; t--) {
      if (tokww[tokww.length - t] != tok2[tok2.length - t]) return false;
    }

    return true;
    }
  }

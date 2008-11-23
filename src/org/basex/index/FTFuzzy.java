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
public final class FTFuzzy extends Index {
  /** Levenshtein reference. */
  final Levenshtein ls = new Levenshtein();
  /** Number of index entries. */
  int indexsize;
  /** Number of ftdata entries. */
  int ftdatasize;
  /** Values file. */
  final Data data;
  /** Index storing each unique token length and pointer
   * on the first token with this length. */
  final DataAccess li;
  /** Index storing each token, its data size and pointer
   * on then data. */
  final DataAccess ti;
  /** Storing pre and pos values for each token. */
  final DataAccess dat;
  /** Token positions. */
  final int[] tp = new int[MAXLEN + 1];
  /** Cache for number of hits and data reference per token. */
  private final FTTokenMap cache;

  /**
   * Constructor, initializing the index structure.
   * @param d data reference
   * @param db name of the database

   * @throws IOException IO Exception
   */
  public FTFuzzy(final Data d, final String db) throws IOException {
    final String file = DATAFTX;
    ti = new DataAccess(db, file + "y");
    dat = new DataAccess(db, file + "z");
    data = d;

    // cache token length index
    li = new DataAccess(db, file + "x");
    for(int i = 0; i < tp.length; i++) tp[i] = -1;
    int is = li.read();
    while(--is >= 0) {
      final int p = li.read();
      tp[p] = li.readInt();
    }
    tp[tp.length - 1] = (int) ti.length();
    cache = new FTTokenMap();
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
    addOccurrences(stats);
    stats.print(tb);
    return tb.finish();
  }


  /**
   * Determines the pointer on a token.
   * 
   * 
   * @param tok token looking for.
   * @param rs right side, -1 as default
   * @param lc flag for converting token from db to lc
   * @return int pointer
   */
  private int getPointerOnTokenCS(final byte[] tok, final int rs, 
      final boolean lc) {
    final int tl = tok.length;
    int l = tp[tl];
    if(l == -1) return -1;

    int r = rs;
    int i = 1;
    if (r == -1) {
      do r = tp[tl + i++]; while(r == -1); 
    }
    
    final int o = tl + 9;

    // binary search
    while(l < r) {
      final int m = l + (r - l) / 2 / o * o;
      final int c = diff(lc ? Token.lc(ti.readBytes(m, m + tl)) 
          : ti.readBytes(m, m + tl), tok);
      if(c == 0) return m;
      else if(c < 0) l = m + o;
      else r = m - o;
    }
    return r == l && eq(lc ? Token.lc(ti.readBytes(l, l + tl)) 
        : ti.readBytes(l, l + tl), tok) ? l : -1;
  }

  
  /**
   * Collects all tokens and their sizes found in the index structure.
   * @param stats statistic reference
   */
  private void addOccurrences(final IndexStats stats) {
    int i = 0;
    while(i < tp.length && tp[i] == -1) i++;
    int p = tp[i];
    int j = i + 1;
    while(j < tp.length && tp[j] == -1) j++;
    
    while(p < tp[tp.length - 1]) {
      if(stats.adding(getDataSize(p, i))) stats.add(ti.readBytes(p, p + i));
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
   * Extracts data from disk and returns it in
   * [[pre1, ..., pres], [pos1, ..., poss]] representation.
   *
   * @param s number of pre/pos values
   * @param p pointer on data
   * @param da DataAccess for reading ftdata
   * @param d data reference 
   * @return  int[][] data
   */
  public static IndexArrayIterator getData(final long p, final int s, 
      final DataAccess da, final Data d) {
    if(s == 0 || p < 0) return IndexArrayIterator.EMP;
    
    if (d.meta.ftittr) {
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
          c++;
          while (c < s && (lpre = da.readNum()) == pre) {
            il.add(da.readNum());
            c++;
          }
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
    } else {
      final int[][] dt = new int[2][s];
      da.cursor(p);
      for(int i = 0; i < s; i++) dt[0][i] = da.readNum();
      for(int i = 0; i < s; i++) dt[1][i] = da.readNum();  
      return new IndexArrayIterator(dt, true);
    }
  }

  /**
   * Performs a fuzzy search for token, with e maximal number
   * of errors e.
   *
   * @param tok token looking for
   * @param k number of errors allowed
   * @return int[][] data
   */
  private IndexIterator getFuzzy(final byte[] tok, final int k) {
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
          it = IndexArrayIterator.merge(getData(getPointerOnData(p, s), 
              getDataSize(p, s), dat, data), it);
        }
        p += s + 9;
      }
    }
    return it;
  }

  /**
   * Get pre- and pos values, stored for token out of index.
   * @param tok token looking for
   * @param cs flag for case sensitive search in query
   * @return iterator
   */
  private IndexArrayIterator get(final byte[] tok, final boolean cs) {
    int p = -1; // = getPointerOnToken(tok);
    int s;
    IndexArrayIterator iai = IndexArrayIterator.EMP;
    if (!data.meta.ftcs) {
        p = getPointerOnTokenCS(Token.lc(tok), p, true);
        if (p > -1) {
          s = getDataSize(p, tok.length);
          iai = getData(getPointerOnData(p, tok.length), s, dat, data);
        }
        return iai;
    } 
    
    p = getPointerOnTokenCS(tok, p, false);
    if (p > -1) {
      s = getDataSize(p, tok.length);
      iai = getData(getPointerOnData(p, tok.length), s, dat, data);
    }
    
    if (!cs) {
      p = getPointerOnTokenCS(Token.lc(tok), p, true);
      if (p > -1) {
        s = getDataSize(p, tok.length);
        iai = IndexArrayIterator.merge(
            getData(getPointerOnData(p, tok.length), s, dat, data), iai);
      }
    }
    return iai;
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
    
    int pw = tok.length;
    
    /*
    IndexArrayIterator iai = null;
    if(ft.wc) {
      pw = indexOf(tok, '.');
      if(pw != -1) iai = getTokenWildCard(lc(tok), pw);
    }
    */

    // get result iterator
    IndexArrayIterator iai = IndexArrayIterator.EMP;
    if(ft.cs || data.meta.ftcs) {
      iai = get(tok, ft.cs);
    } else {
      // check if result was cached
      final int id = cache.id(Token.lc(tok));
      if (id > 0) {
        final int size = cache.getSize(id);
        final long p = cache.getPointer(id);
        return getData(p, size, dat, data);
      } else {
        iai = get(tok, ft.cs);
      }
    }
    
    if (iai == IndexArrayIterator.EMP || !ft.cs || data.meta.ftcs) return iai;
    
    // case sensitive search
    // check real case of each result node
    final FTTokenizer ftdb = new FTTokenizer();
    ftdb.st = ft.st;
    return csDBCheck(iai, data, ftdb, tok, pw);
  }

  /**
   * Performs db-access and checks real case of a token.
   * @param ids full-text ids
   * @param d Data-reference
   * @param ftdb Fulltext Tokenizer
   * @param token token
   * @param pw position of wildcard
   * @return counter
   */
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
              if (pw > b.length 
                  || !eq(Token.substring(token, 0, pw), 
                      Token.substring(b, 0, pw))) {
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

  
  @Override
  public int nrIDs(final IndexToken index) {
    // hack, should find general solution
    final FTTokenizer fto = (FTTokenizer) index;
    if(fto.fz || fto.wc || fto.cs || data.meta.ftcs) return 1;

    // specified ft options are not checked yet...
    final byte[] tok = Token.lc(index.get());
    final int id = cache.id(tok);
    if(id > 0) return cache.getSize(id);

    long p = getPointerOnTokenCS(tok, -1, false);
    
    if (p > -1) {
      final int size = getDataSize(p, tok.length);
      cache.add(tok, size, getPointerOnData(p, tok.length));
      return size;
    } else {
      cache.add(tok, 0, 0);
      return 0;
    }
  }

  @Override
  public synchronized void close() throws IOException {
    li.close();
    ti.close();
    dat.close();
  }

  /*
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

package org.basex.index;

import static org.basex.data.DataText.*;
import static org.basex.Text.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.io.DataAccessPerf;
import org.basex.util.FTTokenizer;
import org.basex.util.IntList;
import org.basex.util.Levenshtein;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.query.xpath.expr.FTUnion;

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
  private final Data data;
  /** Index storing each unique token length and pointer 
   * on the first token with this length. */
  private final DataAccessPerf li;
  /** Index storing each token, its data size and pointer
   * on then data. */
  private final DataAccessPerf ti;
  /** Storing pre and pos values for each token. */ 
  private DataAccessPerf dat;  

  /**
   * Constructor, initializing the index structure.
   * @param d data reference
   * @param db name of the database
 
   * @throws IOException IO Exception
   */
  public Fuzzy(final Data d, final String db) throws IOException {
    final String file = DATAFTX;
    li = new DataAccessPerf(db, file + "x", "TokenLengthIndex");
    ti = new DataAccessPerf(db, file + "y" , "Token");
    dat = new DataAccessPerf(db, file + "z", "FTData");
    data = d;
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
  public int getPointerOnToken(final byte[] tok) {
    int i = 0;
    int is = li.readBytes(0, 1L)[0];
    int ts = li.readBytes(1L, 2L)[0];
    while (i < is && tok.length > ts) {
      i++;
      ts = li.readBytes(1L + i * 5L, 1L + i * 5L + 1L)[0];
    }
    
    if (i == is || ts > tok.length) return -1;
    int l = li.readInt(1L + i * 5L + 1L);
    int r = li.readInt(1L + (i + 1) * 5L + 1L);
    // <SG> m is overwritten without having being touched
    int m = (int) (l + ((int) ((r - l) 
        /// (tok.length * 1L + 8L) / 2)) * (tok.length * 1L + 8L));
        / (tok.length * 1L + 9L) / 2)) * (tok.length * 1L + 9L));
    int res = -2;
    byte[] dtok = new byte[tok.length];
    while (l < r) {
      m = (int) (l + ((int) ((r - l) 
          /// (tok.length * 1L + 8L) / 2)) * (tok.length * 1L + 8L));
          / (tok.length * 1L + 9L) / 2)) * (tok.length * 1L + 9L));
      dtok = ti.readBytes(m, m + dtok.length);
      res = Token.cmp(dtok, tok);
      if (res == 0) return m; 
      //else if (res > 0) l = (int) (m + tok.length * 1L + 8L);
      else if (res > 0) l = (int) (m + tok.length * 1L + 9L);
      //else r = (int) (m - (tok.length * 1L + 8L));
      else r = (int) (m - (tok.length * 1L + 9L));
    }
    
    if (r == l) { 
      if (res == 0) {
        return l;
      }
      dtok = ti.readBytes(l, l + dtok.length);
      res = Token.cmp(dtok, tok);
      if (res == 0) return l; 
      
    }
    return -1;
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
      while (i < indexsize && tokl > ts) {
        i++;
        ts = li.readBytes(1L + i * 5L, 1L + i * 5L + 1L)[0];
      }
      
      if (i == indexsize) return null;

      // back up last pointer on index
      lastIndex = (int) (1L + (i + 1) * 5L);
      return new int[][] {
          // token length, pointer on the first token with this length
          {li.readBytes(1L + i * 5L, 1L + i * 5L + 1L)[0], 
            li.readInt(1L + i * 5L + 1L)}, 
          {li.readBytes(1L + (i + 1) * 5L, 1L + (i + 1) * 5L + 1L)[0], 
              li.readInt(1L + (i + 1) * 5L + 1L)} };
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
  public long getPointerOnData(final int pt, final int lt) {
    //return ti.readInt(pt + lt * 1L);
    return ti.read5(pt + lt * 1L);
  }

  /**
   * Reads the size of ftdata from disk.
   * @param pt pointer on token
   * @param lt length of the token
   * @return size of the ftdata
   */
  public int getDataSize(final int pt, final int lt) {
    return ti.readInt(pt + lt * 1L + 5L);
  }
  
  /**
   * Reads the ftdata from disk.
   * @param p pointer of ftdata
   * @param s size of pre values
   * @return iterator
   */
  private int[][] getData(final long p, final int s) {
    int[][] d = new int[2][s];
    if(data.meta.fcompress) {
      d[0] = dat.readNums(p, s);
      d[1] = dat.readNums(s);
    } else {
      d[0] = dat.readInts(p, p + s * 4L);
      d[1] = dat.readInts(p + s * 4L, p + 2 * s * 4L);
    }
    return d;
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
    int is = li.readBytes(0, 1L)[0];
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
              getData(getPointerOnData(p, ts), getDataSize(p, ts)));
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
    final int p = getPointerOnToken(tok);

    if (p == -1) return IndexIterator.EMPTY;
    int[][] res = getData(getPointerOnData(p, tok.length),
        getDataSize(p, tok.length));

    return new IndexArrayIterator(res[0], res[1]);
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
    
    if(!ft.cs) {
      // index request with pre-values as result
      return get(Token.lc(tok));
    }

    // index request with pre-values and positions as result
    IndexIterator ii = get(Token.lc(tok));
    if(!ii.more()) return null;

    final IntList pre = new IntList();
    final IntList pos = new IntList();
    do {
      pre.add(ii.next());
      ii.more();
      pos.add(ii.next());
    } while(ii.more());
    final int[][] ids = { pre.finish(), pos.finish() };

    byte[] tokenFromDB;
    byte[] textFromDB;
    int[][] rIds = new int[2][ids[0].length];
    int count = 0;
    int readId;

    int i = 0;
    // check real case of each result node
    while(i < ids[0].length) {
      // get date from disk
      // <SG> readId is overwritten again some lines later... 
      readId = ids[0][i];
      textFromDB = data.text(ids[0][i]);
      tokenFromDB = new byte[tok.length];

      System.arraycopy(textFromDB, ids[1][i], tokenFromDB, 0, tok.length);

      readId = ids[0][i];

      // check unique node ones
      while (i < ids[0].length && readId == ids[0][i]) {
        System.arraycopy(textFromDB, ids[1][i], tokenFromDB, 0, tok.length);

        readId = ids[0][i];

        // check unique node ones
        // compare token from db with token from query
        if (Token.eq(tokenFromDB, tok)) {
          rIds[0][count] = ids[0][i];
          rIds[1][count++] = ids[1][i];

          // jump over same ids
          while (i < ids[0].length && readId == ids[0][i]) i++;
          break;
        }
        i++;
      }
    }

    if (count == 0) return null;
    
    int[][] tmp = new int[2][count];
    System.arraycopy(rIds[0], 0, tmp[0], 0, count);
    System.arraycopy(rIds[1], 0, tmp[1], 0, count);
    return new IndexArrayIterator(tmp[0], tmp[1]);
  }
  
  @Override
  public int nrIDs(final IndexToken index) {
    // specified ft options are not checked yet...
    
    final byte[] tok = index.get();
    final int p = getPointerOnToken(Token.lc(tok));
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

    int d = (tok1.length > tok2.length) ? 
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
            getData(getPointerOnData(b[0][1], b[0][0]),
                getDataSize(b[0][1], b[0][0])));
        // b[0][1] += b[0][0] * 1L + 8L;
        b[0][1] += b[0][0] * 1L + 9L;
        //}
      }
      dtok = ti.readBytes(b[0][1], b[0][0] + b[0][1]);
      if (contains(tok, posw, dtok)) dt = FTUnion.calculateFTOr(dt,
          getData(getPointerOnData(b[0][1], b[0][0]),
              getDataSize(b[0][1], b[0][0])));
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

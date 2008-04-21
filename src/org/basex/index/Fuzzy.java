package org.basex.index;

import static org.basex.data.DataText.*;
import static org.basex.Text.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.io.DataAccess;
import org.basex.io.PrintOutput;
import org.basex.util.Levenshtein;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.basex.query.xpath.expr.FTOption;
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
 * Structure of sizes:
 * #indexsize (int)
 * [(byte b, int p), ...] b = first char of a searchsting, p pointer  
 * #indexedtokens (int)
 * [(byte l, int n), ...] l = length of the token, n position in ftdata
 * 
 * Structure of ftdata:
 * [(t,o,k,e,n, C, pre0, ..., preC, pos0,..., posC), ...] 
 * t,o,k,e,n as byte[], C as int, pre and pos as int
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class Fuzzy implements Index {
  /** Number of index entries. */
  int indexsize;
  /** Number of ftdata entries. */
  int ftdatasize;
  /** Values file. */
  final Data data;
  
  /** FTdata lists. */
  private final DataAccess ftdata;
  /** FTdata lists. */
  private final DataAccess sizes;
  /** Index on diskdata. **/
  private int[][] index;
  /** Index offset - number of bytes, used for the index. **/
  private int ios;
  
  /**
   * Constructor, initializing the index structure.
   * @param d data reference
   * @param db name of the database
   * @throws IOException IO Exception
   */
  public Fuzzy(final Data d, final String db)
      throws IOException {
    data = d;
    final String file = DATATXT;
    ftdata = new DataAccess(db, file + "z");
    sizes = new DataAccess(db, file + "x");

    // index size stored as byte
    indexsize = sizes.read4(0);
    index = new int[indexsize][2];
    for (int z = 0; z < indexsize; z++) {
      index[z][0] = sizes.readBytes(4L + z * 5L, 4L + z * 5L + 1)[0];
      index[z][1] = sizes.readInt(4L + z * 5L + 1);
      //System.out.println("i:" + index[z][0] + "," + index[z][1]);
    }
    
    /*
    // index size stored as byte
    indexsize = sizes.read4(0);
    index = new int[indexsize][2];
    for (int z=0; z<indexsize; z++) {
      //System.out.println((sizes.readBytes(4L + z * 5L, 4L + z * 5L + 1)[0]) 
      //    + "," + sizes.readInt(4L + z * 5L + 1));
      index[z][0] = sizes.readBytes(4L + z * 5L, 4L + z * 5L + 1)[0];
      index[z][1] = sizes.readInt(4L + z * 5L + 1);
    }
    index[indexsize-1][0] = Integer.MAX_VALUE;
    // {[v, p]} v value stores as byte, p pointer stored as int
    // data size stored as int
    ftdatasize = sizes.readInt(4L + indexsize * 1L + 4L * indexsize);
    ios = (int) (4L + indexsize * 1L + 4L * indexsize + 4L);
  */
  }

  /** {@inheritDoc} */
  public void info(final PrintOutput out) throws IOException {
    out.println(TEXTINDEX);
    out.println("FUZZY TABLE");
    //out.println("SIZE" + ftdatasize);
    final long l = ftdata.length();
    out.println(SIZEDISK + Performance.formatSize(l, true) + Prop.NL);
  }

  /** {@inheritDoc} */
  public int[] ids(final byte[] tok) {
    int i = 0;
    while (i < index.length && tok.length != (byte) index[i][0]) i++;
    if (i == index.length) return null;
    
    return CTArrayX.getIDsFromData(
        get(tok, index[i][1], index[i + 1][1]));
    
    //return CTArrayX.getIDsFromData(getFuzzy(tok, 3));
    
    /*int i = 0;
    while (i < index.length && tok[0] > (byte) index[i][0]) i++;
    if (i == index.length || index[i][0] > tok[0]) return null;
    return CTArrayX.getIDsFromData(
        getBinary(tok, (int) (index[i][1] * 5L + ios), 
            (int) (index[i + 1][1] * 5L + ios)));
    */
  }

  /**
   * Performes a fuzzy search for token, with e maximal number
   * of errors e.
   * 
   * @param tok token looking for
   * @param e number of errors allowed
   * @return int[][] data
   */
  public int[][] getFuzzy(final byte[] tok, final int e) {
    int[][] dat = null;
    int[][] td;
    byte[] to;
    int p;
    int pe;
    int dif;
    for (int i = 0; i < index.length; i++) {
      dif = (tok.length - index[i][0] < 0) ? 
          index[i][0] - tok.length : tok.length - index[i][0]; 
      if (dif <= e) {
        p = index[i][1];
        pe = index[i + 1][1];
        //System.out.println("p:" + p + " pe:" + pe);
        while(p < pe) {
          to = ftdata.readBytes(p, p + index[i][0]);
          if (calcEQ(to, 0, tok, e)) {
            // read data
            td = new int[2][ftdata.readInt(p + index[i][0])];
            p += index[i][0] + (int) 4L;
            System.arraycopy(ftdata.readInts(p, p + td[0].length * 4L), 
                0, td[0], 0, td[0].length);
            p += td[0].length * 4L;
            System.arraycopy(ftdata.readInts(p, p + td[0].length * 4L), 
                0, td[1], 0, td[0].length);
            p += td[0].length * 4L;
            dat = FTUnion.calculateFTOr(dat, td);
          } else {
            p += index[i][0] + (int) 4L 
            + (ftdata.readInt(p + index[i][0]) * 4L) * 2; 
          }
       }
    }
      }
    return dat;
  }
 
  /**
   * Get token out of index structure.
   * 
   * @param tok token looking for
   * @param pt pointer on first token in index with tok.length
   * @param pe pointer on first token in index wiht tok.length + 1
   * @return data
   */
  public int[][] get(final byte[] tok, final int pt, final int pe) {
    int p = pt;
    byte[] to;
    int[][] d;
    int c;
    while(p < pe) {
      to = ftdata.readBytes(p, p + tok.length);
      c = Token.cmp(to, tok);
      if (c == 0) {
        // read data
        d = new int[2][ftdata.readInt(p + tok.length)];
        System.out.println("d.length:" + d[0].length);
        p += tok.length + (int) 4L;
        System.arraycopy(ftdata.readInts(p, p + d[0].length * 4L), 0, 
            d[0], 0, d[0].length);
        p += d[0].length * 4L;
        System.arraycopy(ftdata.readInts(p, p + d[0].length * 4L), 0, d[1], 
            0, d[0].length);
        return d;
      } else if (c > 0) {
        // read next token from db
        p = (int) (p + tok.length 
            + ftdata.readInt(p + tok.length) * 2 * 4L + 4L);
      } else {
        return null;
      }
    }
    return null;
    
  }
  
  
  
  /**
   * Get token form the fuzzy structure. Uses binary search on the 
   * ordered list.
   * 
   * @param tok byte[]token looking for
   * @param l int left bound
   * @param r int right bound
   * @return int[][] data pre and pos values
   */
  public int[][] getBinaryWS(final byte[] tok, final int l, final int r) {
    int m;
    if (l == r) {
      m = l;
    } else {
      m = (((l + r - 2 * ios) / 2) / (int) 5L * (int) 5L) + ios;
    }
    
    int mtp = sizes.readInt(m + 1L);
    int mts = sizes.readBytes(m, m + 1L)[0];
    
    byte[] b = ftdata.readBytes(mtp, mtp + mts);
    int i = Token.cmp(tok, b);
    
    if (l != r) {
      if (i < 0) return getBinaryWS(tok, (int) (m + 5L), r);
      else if (i > 0) return getBinaryWS(tok, l, (int) (m - 5L));
    }
    
    if (i == 0) {
      // read data from disk
      int[][] d = new int[2][ftdata.readInt(mts + mtp)];
      System.out.println("datasize=" + d[0].length);
      System.arraycopy(ftdata.readInts(mts + mtp + 4L, 
          mts + mtp + 4L + d[0].length * 4L), 
          0, d[0], 0, d[0].length);
      
      System.arraycopy(ftdata.readInts(mts + mtp + 4L + d[0].length * 4L,
          mts + mtp + 4L + d[0].length * 8L), 
          0, d[1], 0, d[0].length);
      return d;
    }
    return null;
  }

  /** {@inheritDoc} */
  public int[][] fuzzyIDs(final byte[] tok, final int ne) {
    return getFuzzy(tok, ne);
  }

  /** {@inheritDoc} */
  public int[][] idPos(final byte[] tok, final FTOption ftO) {
    BaseX.debug("Values: No fulltext option support.");
/*    int i = 0;
    while (i < index.length && tok.length != (byte) index[i][0]) i++;
    if (i == index.length) return null;
    
    return get(tok, index[i][1], index[i + 1][1]);
*/
    return getFuzzy(tok, 3);
    
    /*  int i = 0;
    while (i < index.length && tok[0] > (byte) index[i][0]) i++;
    if (i == index.length || index[i][0] > tok[0]) return null;
    return getBinary(tok, (int) (index[i][1] * 5L + ios), 
        (int) (index[i + 1][1] * 5L + ios));    
  */
  }
  
  /** {@inheritDoc} */
   public int[][]  idPosRange(final byte[] tok0, final boolean itok0, 
       final byte[] tok1, final boolean itok1) {
    BaseX.debug("Words: No fulltext range query support.");
    return null;
   }

  /** {@inheritDoc} */
  public int nrIDs(final byte[] tok) {
    return -1;
  }

   /** {@inheritDoc} */
  public synchronized void close() throws IOException {
    sizes.close();
    ftdata.close();
  }
  
  /**
   * Calculates the equality of tok1 and tok2, with respect to the 
   * number or error e, using the Levenshtein Algorithem.
   * 
   * @param tok1 token1 to compare 
   * @param sp start position in token
   * @param tok2 token2 to compare
   * @param e number of errors allowed
   * @return boolean as result
   */
  public static boolean calcEQ(final byte[] tok1, final int sp, 
      final byte[] tok2, final int e) {
    final int df = (tok1.length - tok2.length < 0) ? 
        tok2.length - tok1.length : 
      tok1.length - tok2.length;
    if (df > e) return false;
    
    int d;
    d = (tok1.length > tok2.length) ? 
        Levenshtein.levenshtein(tok1, sp, tok1.length, tok2, e) : 
      Levenshtein.levenshtein(tok2, sp, tok2.length, tok1, e);
    return d <= e;
    }
}

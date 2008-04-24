package org.basex.index;

import static org.basex.data.DataText.*;
import static org.basex.Text.*;
import java.io.IOException;
import org.basex.core.Progress;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.io.DataOutput;
import org.basex.util.Array;
import org.basex.util.IntArrayList;
import org.basex.util.Performance;
import org.basex.util.Token;

/**
 * This class builds an index for text contents, optimized for fuzzy search,
 * in an ordered table.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 * @author Christian Gruen
 */
public final class FuzzyBuilder extends Progress implements IndexBuilder {
  /** Current parsing value. */
  private int id;
  /** Current parsing value. */
  private int total;
  /** Word parser. */
  private final FTTokenizer wp = new FTTokenizer();
  /** Tokens, saved temp. in building process. **/
  public IntArrayList[] token;
  /** Tokens, saved temp. in building process. **/
  private IntArrayList[] ftpre;
  /** Tokens, saved temp. in building process. **/
  private IntArrayList[] ftpos;
  
  /**
   * Builds the index structure and returns an index instance.
   * The building process is divided in 2 steps:
   * a) 
   *    fill DataOutput(db, f + 'x') looks like:
   *    [l, p] ... where l is the length of a token an p the pointer of
   *                the first token with length l; there's an entry for 
   *                each token length [byte, int]
   *    fill DataOutput(db, f + 'y') looks like:
   *    [t0, t1, ... tl, z, s] ... where t0, t1, ... tl are the byte values 
   *                           of the token (byte[l]); z is the pointer on
   *                           the data entries of the token (int) and s is
   *                           the number of pre values, saved in data (int)
   *
   * b)                            
   *    fill DataOutput(db, f + 'z') looks like:
   *    [pre0, ..., pres, pos0, pos1, ..., poss] where pre and pos are the 
   *                          ft data [int[]]             
   * @param data data reference
   * @return index instance
   * @throws IOException IO Exception
   */
  public Fuzzy build(final Data data) throws IOException {
    // to save the tokens
    token = new IntArrayList[Token.MAXLEN];
    token[0] = new IntArrayList();
    token[0].add(new int[]{0});
    ftpre = new IntArrayList[Token.MAXLEN];
    ftpre[0] = new IntArrayList();
    ftpre[0].add(new int[]{0});
    ftpos = new IntArrayList[Token.MAXLEN];
    ftpos[0] = new IntArrayList();
    ftpos[0].add(new int[]{0});   

    Performance p = new Performance();
    
    total = data.size;
    for(id = 0; id < total; id++) {
      checkStop();
      if(data.kind(id) == Data.TEXT) index(data.text(id), id);
    }
       
    if(Prop.debug) {
      Performance.gc(5);
      System.out.println("Indexed: " + Performance.getMem() + ", " + p);
    }

    int isize = token[0].list[0][0];
    final String db = data.meta.dbname;
    final String f = DATAFTX;
    
    DataOutput outi = new DataOutput(db, f + 'x');
    DataOutput outt = new DataOutput(db, f + 'y');
    DataOutput outd = new DataOutput(db, f + 'z');
 
    // write index size
    outi.write((byte) (isize + 1));
    int[][] ind = new int[isize + 1][2];
    int c = 0, tr = 0, dr = 0, j = 1;
    for (; j < token.length; j++) {
      if (c == isize) break;
      
      if (token[j] != null) {
        int t = 0;
        while(t < token[j].list.length && token[j].list[t] != null) {
          if (t == 0) {
            // write index with tokenlength
            outi.write((byte) j);
            //and pointer on first token with this length
            outi.writeInt(tr);
            ind[c][0] = j;
            ind[c][1] = tr;            
          }

          // write token value
          for (int k = 0; k < token[j].list[t].length - 1; k++) { 
            outt.write((byte) token[j].list[t][k]); 
          }
          // write pointer on data
          outt.writeInt(dr);
          // write data size
          int ds = token[j].list[t][token[j].list[t].length - 1];
          outt.writeInt(ds);
          // write pre and pos values
          for(int d = 0; d < ds; d++) outd.writeInt(ftpre[j].list[t][d]);
          for(int d = 0; d < ds; d++) outd.writeInt(ftpos[j].list[t][d]);
          dr += 8L * ds;
          tr += ind[c][0] + 8L;
          t++;
        }
        c++;
      }
    }
    
    outi.write((byte) (j - 1));
    outi.writeInt((int) (tr - j - 7L));
    ind[c][0] = j - 1;
    ind[c][1] = (int) (tr - j - 7L);

    token = null;
    ftpre = null;
    ftpos = null;
    
    outi.close();
    outt.close();
    outd.close();

    if(Prop.debug) {
      Performance.gc(5);
      System.out.println("Written: " + Performance.getMem() + ", " + p);
    }

    return new Fuzzy(data.meta.dbname);
  }
  
  /**
   * Extracts and indexes words from the specified byte array.
   * @param tok token to be extracted and indexed
   * @param pre int pre value
   */
  private void index(final byte[] tok, final int pre) {
    wp.init(tok);
    while(wp.more()) index(wp.finish(), pre, wp.off());
  }
 
  /**
   * Indexes a single token.
   * @param tok token to be indexed
   * @param pre pre value of the token
   * @param pos position value of the token
   */
  private void index(final byte[] tok, final int pre, final int pos) {
    int tl = tok.length;
    if (token[tl] == null) {
      token[0].list[0][0]++;
      IntArrayList ial = new IntArrayList();
      int[] itok = new int[tl + 1];
      for (int t = 0; t < tl; t++) itok[t] = tok[t];
      itok[tl] = 1;
      ial.add(itok);
      token[tl] = ial;
      ftpre[tl] = new IntArrayList();
      ftpre[tl].add(new int[]{pre});
      ftpos[tl] = new IntArrayList();
      ftpos[tl].add(new int[]{pos});
    } else {
      int[] itok = new int[tl];
      for (int t = 0; t < tl; t++) itok[t] = tok[t];
      int m = token[tl].addSorted(itok, itok.length);
      if (token[tl].found) {
        token[tl].list[m][tl]++;
        int n = token[tl].list[m][token[tl].list[m].length - 1] - 1;
        if (n == ftpre[tl].list[m].length) {
          ftpre[tl].list[m] = Array.extend(ftpre[tl].list[m]);
          ftpos[tl].list[m] = Array.extend(ftpos[tl].list[m]);
        }
        ftpre[tl].list[m][n] = pre;
        ftpos[tl].list[m][n] = pos;
      } else {
        ftpre[tl].addAt(new int[]{pre}, m);
        ftpos[tl].addAt(new int[]{pos}, m);          
      }
    }
  }

  @Override
  public String tit() {
    return PROGINDEX;
  }

  @Override
  public String det() {
    return INDEXFTX;
  }

  @Override
  public double prog() {
    return (double) id / total;
  }
}

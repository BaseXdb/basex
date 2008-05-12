package org.basex.index;

import static org.basex.data.DataText.*;
import static org.basex.Text.*;
import java.io.IOException;
import org.basex.core.Progress;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.io.DataOutput;
import org.basex.util.Array;
import org.basex.util.ByteArrayList;
import org.basex.util.IntArrayList;
import org.basex.util.Performance;

/**
 * This class builds an index for text contents in a compressed trie.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 * @author Christian Gruen
 */
public final class FTBuilder extends Progress implements IndexBuilder {
  /** Word parser. */
  private final FTTokenizer wp = new FTTokenizer();
  /** CTArray for tokens. **/
  private CTArrayNew index;
  /** Current parsing value. */
  private int id;
  /** Current parsing value. */
  private int total;
  /** Tokens, saved temp. in building process. **/
  private ByteArrayList[] token;
  /** Tokens, saved temp. in building process. **/
  private IntArrayList[] ftpre;
  /** Tokens, saved temp. in building process. **/
  private IntArrayList[] ftpos;

  /**
   * Builds the index structure and returns an index instance.
   * @param data data reference
   * @return index instance
   * @throws IOException IO Exception
   */
  public WordsCTANew build(final Data data) throws IOException {
    int s = 0;
    index = new CTArrayNew();
    if (index.bl) {
      token = new ByteArrayList[128];
      token[0] = new ByteArrayList();
      token[0].add(new byte[]{0});
      ftpre = new IntArrayList[128];
      ftpre[0] = new IntArrayList();
      ftpre[0].add(new int[]{0});
      ftpos = new IntArrayList[128];
      ftpos[0] = new IntArrayList();
      ftpos[0].add(new int[]{0});   
    }
    
    total = data.size;
    for(id = 0; id < total; id++) {
      checkStop();
      if(data.kind(id) == Data.TEXT) index(data.text(id));
    }
    
    if (Prop.debug) {
      System.out.println("Trie in Hauptspeicher gehalten:");
      Performance.gc(5);
      System.out.println(Performance.getMem());
    }
    
    if (index.bl) {
      bulkLoad(total);
    }

    index.finish();
    
    if (Prop.debug) {
      System.out.println("Trie in komprimierte Form überführt.");
      Performance.gc(5);
      System.out.println(Performance.getMem());
    }
    
    final String db = data.meta.dbname;
    
    // save each node: l, t1, ..., tl, u, n1, v1, ..., nu, vu, s, p
    // l = length of the token t1, ..., tl
    // u = number of next nodes n1, ..., nu
    // v1= the first byte of each token n1 points, ...
    // s = size of pre values saved at pointer p
    // [byte, byte[l], byte, int, byte, ..., int, int]
    DataOutput outN = new DataOutput(db, DATAFTX + 'a');
    // ftdata is stored here, with pre1, ..., preu, pos1, ..., posu
    DataOutput outD = new DataOutput(db, DATAFTX + 'b');
    // each nodeetries size is stored here
    DataOutput outS = new DataOutput(db, DATAFTX + 'c');

    // document contains any textnodes -> empty index created;
    //only root node is kept
    if(index != null && index.count != 1) {
      // index.next[i] : [p, n1, ..., s, d]
      // index.tokesn[p], index.next[n1], ..., index.pre[d]

      // first root node
      // write token size as byte
      outN.write((byte) 1);
      // write token
      outN.write((byte) -1);
      // write next pointer
      int j = 1;
      for (; j < index.next[0].length - 2; j++) {
        outN.writeInt(index.next[0][j]); // pointer
        // first char of next node
        outN.write(index.tokens[index.next[index.next[0][j]][0]][0]);
      }
      
      outN.writeInt(index.next[0][j]); // data size
      outN.writeInt(-1); // pointer on data - root has no data
      outS.writeInt(s);
      s += 2L + (index.next[0].length - 3) * 5L + 8L;
      // all other nodes
      for (int i = 1; i < index.next.length; i++) {
        // write token size as byte
        outN.write((byte) index.tokens[index.next[i][0]].length);
        // write token
        outN.write(index.tokens[index.next[i][0]]);
        // write next pointer
        j = 1;
        for (; j < index.next[i].length - 2; j++) {
          outN.writeInt(index.next[i][j]); // pointer
          // first char of next node
          outN.write(index.tokens[index.next[index.next[i][j]][0]][0]);
        }
        outN.writeInt(index.next[i][j]); // data size
        if (index.next[i][j] == 0 && index.next[i][j + 1] == 0) {
          // node has no data
          outN.writeInt(index.next[i][j + 1]);
        } else {
          // write data
          // <SG> int -> long...
          outN.writeInt((int) outD.size()); // pointer on data
          outD.writeInts(index.pre[index.next[i][j + 1]]); // pre values
          outD.writeInts(index.pos[index.next[i][j + 1]]); // pos values
        }
        outS.writeInt(s);
        s += 1L + index.tokens[index.next[i][0]].length * 1L 
             + (index.next[i].length - 3) * 5L + 8L;
      }
    }
    
    outS.writeInt(s);
    outD.close();
    outN.close();
    outS.close();
    return new WordsCTANew(db);
  }

  /**
   * Adds the data to each token.
   * @param isize size of the index
   */
  private void bulkLoad(final int isize)  {   
    // to save the tokens
    int c = 0;
    int[] pres;
    int[] poss;
    for (int j = 1; j < token.length; j++) {
      if (c == isize) break;
      
      if (token[j] != null) {
        int t = 0;
        while(t < token[j].list.length && token[j].list[t] != null) {
          pres = new int[ftpre[j].list[t][ftpre[j].list[t].length - 1]];
          poss = new int[ftpre[j].list[t][ftpre[j].list[t].length - 1]];
          System.arraycopy(ftpre[j].list[t], 0, pres, 0, pres.length);
          System.arraycopy(ftpos[j].list[t], 0, poss, 0, poss.length);
          index.index(token[j].list[t], new int[][]{pres, poss});
          ftpre[j].list[t] = pres;
          ftpos[j].list[t] = poss;
          pres = null;
          poss = null;
          t++;
        }  
      }
    }
  }

  /**
   * Extracts and indexes words from the specified byte array.
   * @param tok token to be extracted and indexed
   */
  private void index(final byte[] tok) {
    wp.init(tok);
    while(wp.more()) index();
  }

  /**
   * Indexes a single token and returns its unique id.
   */
  private void index() {
    final byte[] tok = wp.finish();
    final int pos = wp.off();
    if (index.bl) index(tok, id, pos);
    else index.index(tok, id, pos);

    /**cont = Num.add(cont, tok);
    BaseX.debug("cont:" + Token.toString(cont));
    **/
  }

  /**
   * Indexes a single token.
   * @param tok token to be indexed
   * @param pre pre value of the token
   * @param pos value position value of the token
   */
  private void index(final byte[] tok, final int pre, final int pos) {
    if (token[tok.length] == null) {
      total++;
      ByteArrayList bal = new ByteArrayList();
      bal.add(tok);
      token[tok.length] = bal;
      ftpre[tok.length] = new IntArrayList();
      ftpre[tok.length].add(new int[]{pre, 1});
      ftpos[tok.length] = new IntArrayList();
      ftpos[tok.length].add(new int[]{pos});
    } else {
      int m = token[tok.length].addSorted(tok, tok.length);
      if (token[tok.length].found) {
        int n = ftpre[tok.length].list[m][ftpre[tok.length].list[m].length - 1];
        if (n == ftpre[tok.length].list[m].length - 1) {
          int[] tmp = Array.resize(ftpre[tok.length].list[m], 
              ftpre[tok.length].list[m].length, 
              (ftpos[tok.length].list[m].length << 1) + 1);
          tmp[tmp.length - 1] = tmp[ftpre[tok.length].list[m].length - 1] + 1;
          tmp[ftpre[tok.length].list[m].length - 1] = pre;
          ftpre[tok.length].list[m] = tmp;
          
          tmp = Array.resize(ftpos[tok.length].list[m], 
              ftpos[tok.length].list[m].length, 
              ftpos[tok.length].list[m].length << 1);
          tmp[n] = pos;
          ftpos[tok.length].list[m] = tmp;
        } else {
          ftpre[tok.length].list[m] [n] = pre;
          ftpos[tok.length].list[m] [n] = pos;
          ftpre[tok.length].list[m] [ftpre[tok.length].list[m].length - 1]++; 
        }  
      } else {
        ftpre[tok.length].addAt(new int[]{pre, 1}, m);
        ftpos[tok.length].addAt(new int[]{pos}, m);          
      }
    }
  }
  
  /*
   * Index a token. 
   * @param tok token to index
   * @param pre pre value of the token
   * @param pos pos value of the token
  private void index2(final byte[] tok, final int pre, final int pos) {
    if (token[tok.length] == null) {
      total++;
      ByteArrayList bal = new ByteArrayList();
      bal.add(tok);
      token[tok.length] = bal;
      ftpre[tok.length] = new IntArrayList();
      ftpre[tok.length].add(new int[]{pre});
      ftpos[tok.length] = new IntArrayList();
      ftpos[tok.length].add(new int[]{pos});
    } else {
      int m = token[tok.length].addSorted(tok, tok.length);
      if (token[tok.length].found) {
        int[] tmp = new int[ftpre[tok.length].list[m].length + 1];
        System.arraycopy(ftpre[tok.length].list[m], 0, tmp, 0, tmp.length - 1);
        tmp[tmp.length - 1] = pre;
        ftpre[tok.length].list[m] = tmp;
        tmp = new int[ftpos[tok.length].list[m].length + 1];
        System.arraycopy(ftpos[tok.length].list[m], 0, tmp, 0, tmp.length - 1);
        tmp[tmp.length - 1] = pos;
        ftpos[tok.length].list[m] = tmp;
      } else {
        ftpre[tok.length].addAt(new int[]{pre}, m);
        ftpos[tok.length].addAt(new int[]{pos}, m);          
      }
    }
  }
   */
  
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

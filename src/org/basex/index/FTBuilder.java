package org.basex.index;

import static org.basex.data.DataText.*;
import static org.basex.Text.*;
import java.io.IOException;
import org.basex.core.Progress;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.io.DataOutput;
import org.basex.util.FTTokenizer;
import org.basex.util.Num;
import org.basex.util.Performance;
import org.basex.util.Token;

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
  /** Hash structure for temporarily saving the tokens. */
  private FZHash hash;
  
  /**
   * Builds the index structure and returns an index instance.
   * @param data data reference
   * @return index instance
   * @throws IOException IO Exception
   */
  public WordsCTANew build(final Data data) throws IOException {
    data.meta.fcompress = Prop.fcompress;
    wp.cs = data.meta.ftcs;
    int s = 0;
    index = new CTArrayNew(128, data.meta.ftcs);
    index.bl |= data.meta.filesize > 1073741824;
    if(index.bl) {
      if (wp.cs) {
        // bulk loader doesn't support case sensitivity
        index.bl = false; 
      } else hash = new FZHash();
    }
        
    total = data.size;
    for(id = 0; id < total; id++) {
      checkStop();
      if(data.kind(id) == Data.TEXT) index(data.text(id));
    }
    
    if(Prop.debug) {
      System.out.println("Hash in Hauptspeicher gehalten:");
      Performance.gc(5);
      System.out.println(Performance.getMem());
    }
    
    final String db = data.meta.dbname;
    DataOutput outD = new DataOutput(db, DATAFTX + 'b');
    if(index.bl) {
      bulkLoad(outD);
    }
    
    if(Prop.debug) {
      System.out.println("Hash und Trie in Hauptspeicher gehalten:");
      Performance.gc(5);
      System.out.println(Performance.getMem());
    }

    hash = null;
    index.finish();
    
    if(Prop.debug) {
      System.out.println("Trie in komprimierte Form überführt.");
      Performance.gc(5);
      System.out.println(Performance.getMem());
    }
    
    // save each node: l, t1, ..., tl, n1, v1, ..., nu, vu, s, p
    // l = length of the token t1, ..., tl
    // u = number of next nodes n1, ..., nu
    // v1= the first byte of each token n1 points, ...
    // s = size of pre values saved at pointer p
    // [byte, byte[l], byte, int, byte, ..., int, long]
    DataOutput outN = new DataOutput(db, DATAFTX + 'a');
    // ftdata is stored here, with pre1, ..., preu, pos1, ..., posu
    //DataOutput outD = new DataOutput(db, DATAFTX + 'b');
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
      //System.out.print("1,-1,");
      // write token
      outN.write((byte) -1);
      // write next pointer
      int j = 1;
      for (; j < index.next[0].length - 2; j++) {
        //System.out.print(index.next[0][j] + "," + 
        //(char) index.tokens[index.next[index.next[0][j]][0]][0] + ",");
        outN.writeInt(index.next[0][j]); // pointer
        // first char of next node
        outN.write(index.tokens[index.next[index.next[0][j]][0]][0]);
      }
      //System.out.print("0,-1");
      //System.out.println();
      
      outN.writeInt(index.next[0][j]); // data size
      outN.write5(-1); // pointer on data - root has no data
      outS.writeInt(s);
      s += 2L + (index.next[0].length - 3) * 5L + 9L;
      int lp;
      int[] tmp;
      // all other nodes
      for (int i = 1; i < index.next.length; i++) {
        //System.out.println("id:" + i);
        // check pointer on data needs 1 or 2 ints
        lp = (index.next[i][index.next[i].length - 2] > -1) ? 
            0 : -1;
        // write token size as byte
    //System.out.print((byte) index.tokens[index.next[i][0]].length + ",");
        outN.write((byte) index.tokens[index.next[i][0]].length);
        // write token
        outN.write(index.tokens[index.next[i][0]]);
    //System.out.print(new String(index.tokens[index.next[i][0]]) + ",");
        // write next pointer
        j = 1;
        for (; j < index.next[i].length - 2 + lp; j++) {
          //System.out.print(index.next[i][j] +",");
          outN.writeInt(index.next[i][j]); // pointer
          // first char of next node
          outN.write(index.tokens[index.next[index.next[i][j]][0]][0]);
          //System.out.print((char)
          //index.tokens[index.next[index.next[i][j]][0]][0] + ",");
        }
        outN.writeInt(index.next[i][j]); // data size
        //System.out.print(index.next[i][j] + ",");
        if (index.next[i][j] == 0 && index.next[i][j + 1] == 0) {
          // node has no data
          outN.write5(index.next[i][j + 1]);
          //System.out.print(index.next[i][j + 1]);
        } else {
          // write pointer on data
          if (index.bl) {
            if (lp == 0) {
              outN.write5(index.next[i][j + 1]);
            } else {
              tmp = new int[2];
              System.arraycopy(index.next[i], index.next[i].length - 2, 
                  tmp, 0, tmp.length);
              outN.write5(Token.intArrayToLong(tmp));
            }
          } else {
            writeData(outD, index.pre[index.next[i][index.next[i].length - 1]]);
            writeData(outD, index.pos[index.next[i][index.next[i].length - 1]]);
          }
        }
        //System.out.println();
        outS.writeInt(s);
        s += 1L + index.tokens[index.next[i][0]].length * 1L 
             //+ (index.next[i].length - 3) * 5L + 9L;
        + (index.next[i].length - 3 + lp) * 5L + 9L;
      }
    }
    
    if (!index.bl) {
      // write data
      for (int i = 0; i < index.pre.length; i++) {
        writeData(outD, index.pre[i]);
        writeData(outD, index.pos[i]);
      }
    }
    
    outS.writeInt(s);
    outD.close();
    outN.close();
    outS.close();    
    return new WordsCTANew(data, db);
  }
  
  
  /**
   * Adds the data to each token.
   * @param outD DataOutput
   * @throws IOException IOEXception
   */
  private void bulkLoad(final DataOutput outD)  throws IOException {   
    // ftdata is stored here, with pre1, ..., preu, pos1, ..., posu
    hash.init();
    long c;
    int ds, p;
    byte[] val, tok;
    while(hash.more()) {
      p = hash.next();
      tok = hash.key();
      //System.out.println("<w>" + new String(tok) + "</w>");
      ds = hash.ns[p];
      c = outD.size();
      if (Prop.fcompress) {
        val = Num.finish(hash.pre[p]);
        for (int z = 4; z < val.length; z++) outD.write(val[z]); 
        val = Num.finish(hash.pos[p]);
        for (int z = 4; z < val.length; z++) outD.write(val[z]);
      } else {
        val = hash.pre[p];
        for(int v = 0, ip = 4; v < ds; ip += Num.len(val, ip), v++) {
          outD.writeInt(Num.read(val, ip));
        }
        val = hash.pos[p];
        for(int v = 0, ip = 4; v < ds; ip += Num.len(val, ip), v++) {
          outD.writeInt(Num.read(val, ip));
        }
      }
      index.insertSorted(tok, ds, c);
    }
    //System.out.println("</words>");
   }
  
  /**
   * Writes data to output stream.
   * 
   * @param out DataOutput  stream for the data
   * @param d data to write
   * @throws IOException File not found
   */
  private void writeData(final DataOutput out, final int[] d) 
    throws IOException {
    if (Prop.fcompress) {
      byte[] val = Num.create(d);
      for (int z = 4; z < val.length; z++) out.write(val[z]); 
    } else {
      out.writeInts(d);
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
    final byte[] tok = wp.get();
    if(tok.length > Token.MAXLEN) return;
    
    final int pos = wp.pos;
    if(index.bl) index(tok, id, pos);
    else index.index(tok, id, pos);
  }

  /**
   * Indexes a single token.
   * @param tok token to be indexed
   * @param pre pre value of the token
   * @param pos value position value of the token
   */
  private void index(final byte[] tok, final int pre, final int pos) {
    hash.index(tok, pre, pos);
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

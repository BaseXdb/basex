package org.basex.index;

import static org.basex.Text.*;
import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.core.Progress;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.io.DataOutput;
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
  /** CTArray for tokens. */
  private FTArray index;
  /** Current parsing value. */
  private int id;
  /** Current parsing value. */
  private int total;
  /** Hash structure for temporarily saving the tokens. */
  private FTHash hash;

  /**
   * Builds the index structure and returns an index instance.
   * @param data data reference
   * @return index instance
   * @throws IOException IO Exception
   */
  public FTTrie build(final Data data) throws IOException {
    wp.cs = data.meta.ftcs;
    int s = 0;
    index = new FTArray(128, data.meta.ftcs);
    index.bl |= data.meta.filesize > 1073741824;
    if(index.bl) {
      if (wp.cs) {
        // bulk loader doesn't support case sensitivity
        index.bl = false;
      } else hash = new FTHash();
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
    final DataOutput outPre = new DataOutput(db, DATAFTX + 'b');
    if(index.bl) {
      bulkLoad(outPre, data.meta.ftittr);
    }

    if(Prop.debug) {
      System.out.println("Hash und Trie in Hauptspeicher gehalten:");
      Performance.gc(5);
      System.out.println(Performance.getMem());
    }

    hash = null;

    if(Prop.debug) {
      System.out.println("Trie in komprimierte Form überführt.");
      Performance.gc(5);
      System.out.println(Performance.getMem());
    }

    final byte[][] tokens = index.tokens.list;
    final int[][] next = index.next.list;
    final int[][] pre = index.pre.list;
    final int[][] pos = index.pos.list;

    // save each node: l, t1, ..., tl, n1, v1, ..., nu, vu, s, p
    // l = length of the token t1, ..., tl
    // u = number of next nodes n1, ..., nu
    // v1= the first byte of each token n1 points, ...
    // s = size of pre values saved at pointer p
    // [byte, byte[l], byte, int, byte, ..., int, long]
    final DataOutput outN = new DataOutput(db, DATAFTX + 'a');
    // ftdata is stored here, with pre1, ..., preu, pos1, ..., posu
    // each node entries size is stored here
    final DataOutput outS = new DataOutput(db, DATAFTX + 'c');

    // document contains any text nodes -> empty index created;
    // only root node is kept
    if(index.count != 1) {
      // index.next[i] : [p, n1, ..., s, d]
      // index.tokens[p], index.next[n1], ..., index.pre[d]

      // first root node
      // write token size as byte
      outN.write((byte) 1);
      // write token
      outN.write((byte) -1);
      // write next pointer
      int j = 1;
      for (; j < next[0].length - 2; j++) {
        outN.writeInt(next[0][j]); // pointer
        // first char of next node
        outN.write(tokens[next[next[0][j]][0]][0]);
      }

      outN.writeInt(next[0][j]); // data size
      outN.write5(-1); // pointer on data - root has no data
      outS.writeInt(s);
      s += 2L + (next[0].length - 3) * 5L + 9L;
      // all other nodes
      final int il = index.next.size;
      for (int i = 1; i < il; i++) {
        // check pointer on data needs 1 or 2 ints
        //int lp = (next[i][next[i].length - 2] > -1) ? 0 : -1;
        int lp = (next[i][next[i].length - 1] > -1) ? 0 : -1;
        // write token size as byte
        outN.write((byte) tokens[next[i][0]].length);
        // write token
        outN.write(tokens[next[i][0]]);
        // write next pointer
        j = 1;
        for (; j < next[i].length - 2 + lp; j++) {
          outN.writeInt(next[i][j]); // pointer
          // first char of next node
          outN.write(tokens[next[next[i][j]][0]][0]);
        }
        outN.writeInt(next[i][j]); // data size
        if (next[i][j] == 0 && next[i][j + 1] == 0) {
          // node has no data
          outN.write5(next[i][j + 1]);
        } else {
          // write pointer on data
          if (index.bl) {
            if (lp == 0) {
              outN.write5(next[i][j + 1]);
            } else {
              outN.write5(FTArray.toLong(next[i], next[i].length - 2));
            }
          } else {
            writeData(outPre, pre[next[i][next[i].length - 1]]);
            writeData(outPre, pos[next[i][next[i].length - 1]]);
          }
        }
        outS.writeInt(s);
        s += 1L + tokens[next[i][0]].length * 1L
        + (next[i].length - 3 + lp) * 5L + 9L;
      }
    }

    if (!index.bl) {
      // write data
      final int il = index.pre.size;
      for (int i = 0; i < il; i++) {
        writeData(outPre, pre[i]);
        writeData(outPre, pos[i]);
      }
    }

    outS.writeInt(s);
    outPre.close();
    outN.close();
    outS.close();
    return new FTTrie(data, db);
  }
  
  /**
   * Converts an int-array to a long value.
   * @param i int-array with the long value
   * @param p position to read from
   * @return long value
   */
  public static long toLong(final int[] i, final int p) {
    return ((long) -i[p] << 31) | i[p + 1];
  }

  /**
   * Adds the data to each token.
   * @param outPre DataOutput
   * @param ittr boolean itterator optimated storage
   * @throws IOException IOEXception
   */
  private void bulkLoad(final DataOutput outPre, 
      final boolean ittr)  throws IOException {
    hash.init();
    long cpre;
    int ds, p, lpre, lpos, spre, spos;
    byte[] tok, vpre, vpos;
    
    while(hash.more()) {
      p = hash.next();
      tok = hash.key();
      ds = hash.ns[p];
      cpre = outPre.size();
      
      vpre = hash.pre[p];
      vpos = hash.pos[p];
      lpre = 4;
      lpos = 4;
      spre = Num.size(vpre);
      spos = Num.size(vpos);
      
      if (ittr) {
        // ftdata is stored here, with pre1, pos1, ..., preu, posu 
        while(lpre < Num.size(vpre) && lpos < Num.size(vpos)) {
          int z = 0;
          while (z < Num.len(vpre, lpre)) {
            outPre.write(vpre[lpre + z++]);
          }
          lpre += z;
          z = 0;
          while (z < Num.len(vpos, lpos)) {
            outPre.write(vpos[lpos + z++]);
          }
          lpos += z;
        }
      } else {
        // ftdata is stored here, with pre1, ..., preu in outPre and 
        // pos1, ..., posu in outPos
        for (int z = 4; z < spre; z++) outPre.write(vpre[z]);
        for (int z = 4; z < spos; z++) outPre.write(vpos[z]);
      }
      index.insertSorted(tok, ds, cpre);
    }
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
    final byte[] val = Num.create(d);
    for (int z = 4; z < val.length; z++) out.write(val[z]);
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

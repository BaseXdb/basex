package org.basex.index;

import static org.basex.data.DataText.*;
import static org.basex.Text.*;
import java.io.IOException;
import org.basex.core.Progress;
import org.basex.data.Data;
import org.basex.data.DiskData;
import org.basex.io.DataOutput;
import org.basex.util.Token;


/**
 * This class builds an index for text contents in a compressed trie.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 * @author Christian Gruen
 */
public final class FTBuilder extends Progress implements IndexBuilder {
  /** Temporary token reference. */
  private byte[] tmptok;
  /** Temporary token start. */
  private int tmps;
  /** Temporary token end. */
  private int tmpe;
  /** Temporary token length. */
  private int tmpl;

  /** CTArray for tokens. **/
  private CTArray index;

  /** Current parsing value. */
  private int id;
  /** Current parsing value. */
  private int total;

  /**
   * Builds the index structure and returns an index instance.
   * @param data data reference
   * @return index instance
   * @throws IOException IO Exception
   */
  public WordsCTA build(final Data data) throws IOException {
  //public Fuzzy build(final Data data) throws IOException {
    index = new CTArray();
    total = data.size;
    for(id = 0; id < total; id++) {
      if(stopped()) throw new IOException(CANCELCREATE);
      if(data.kind(id) == Data.TEXT) index(data.text(id), id);
    }

    // document contains any textnodes -> empty index created;
    //only root node is kept
    final String db = data.meta.dbname;
    if(index != null && index.countNodes != 1) {
      final CTArrayX indexX = new CTArrayX(index);
      indexX.finish();
      indexX.doTransport();
      index = null;

      /*System.out.println("***** direkt vor dem schreiben auf platte: ******");
      
      System.out.println("sizeData:" + indexX.sizeData.length);
      System.out.println("data.length:" + indexX.data.length);
      for (int i =0; i<indexX.sizeData.length; i++){
        System.out.print(indexX.sizeData[i] + ":");
        for (int j=0; j<indexX.data[i].length; j++) {
          System.out.print(indexX.data[i][j] + ",");
        }
        System.out.println();
      }
           
      System.out.println("*******************");
      */
      
      // save size for each node
      DataOutput out = new DataOutput(db, DATAFTX + 'x');
      out.writeStructureWithOffsets(indexX.sizeNodes);
      out.close();

      // save size for each data element
      out = new DataOutput(db, DATAFTX + 'y');
      out.writeStructureWithOffsets(indexX.sizeData);
      out.close();

      // save node elements
      out = new DataOutput(db, DATAFTX + 'v');
      out.writeBytesArrayFlat(indexX.nodes);
      out.close();

      // save data values
      out = new DataOutput(db, DATAFTX + 'd');
      out.writeIntArray(indexX.data);
      out.close();
      //FuzzyBuilder fb = new FuzzyBuilder(indexX);
      //return fb.build(data);
      
    }
    //return null;
    return new WordsCTA(db, (DiskData) data);
  }

  /**
   * Returns current progress value.
   * @return progress information
   */
  public int value() {
    return id;
  }

  /**
   * Extracts and indexes words from the specified byte array.
   * @param tok token to be extracted and indexed
   * @param pre pre value
   */
  private void index(final byte[] tok, final int pre) {
    tmptok = tok;
    tmpe = -1;
    tmpl = tok.length;
    while(parse()) index(pre);
  }

  /**
   * Indexes a single token and returns its unique id.
   * @param pre pre value
   */
  private void index(final int pre) {
    if(tmpe - tmps > Token.MAXLEN) return;

    final byte[] tok = new byte[tmpe - tmps];
    for(int t = 0; t < tok.length; t++) {
      tok[t] = (byte) Token.ftNorm(tmptok[tmps + t]);
    }
    index.index(tok, pre, tmps);

    /**cont = Num.add(cont, tok);
    BaseX.debug("cont:" + Token.toString(cont));
    **/
  }

  /**
   * Parses the input byte array and calculates start and end positions
   * for single words. False is returned as soon as all tokens are parsed.
   * @return true if more tokens exist
   */
  private boolean parse() {
    tmps = -1;
    while(++tmpe <= tmpl) {
      if(tmps == -1) {
        if(tmpe < tmpl && Token.ftChar(tmptok[tmpe])) tmps = tmpe;
      } else if(tmpe == tmpl || !Token.ftChar(tmptok[tmpe])) {
        return true;
      }
    }
    tmptok = null;
    return false;
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

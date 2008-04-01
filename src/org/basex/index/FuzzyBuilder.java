package org.basex.index;

import static org.basex.data.DataText.*;
import static org.basex.Text.*;
import java.io.IOException;
import org.basex.core.Progress;
import org.basex.data.Data;
import org.basex.io.DataOutput;
import org.basex.util.IntArrayList;

/**
 * This class builds an index for text contents, optimized for fuzzy search,
 * in an ordered table.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
public final class FuzzyBuilder extends Progress implements IndexBuilder {
  /** IDs of a token entry. */ // [l,t,o,k,e,n,pre1,pre2,...,pos1,pos2,...]
  private int[][] ftdata;
  /** CTArrayX used to fill the fuzzy index. **/
  private CTArrayX cta;
  /** Runvariable over the index. **/
  private int i;
  
  /**
   * Constructor.
   * @param ctArrayX index used to create fuzzy index.
   */
  public FuzzyBuilder(final CTArrayX ctArrayX) {
    cta = ctArrayX;
  }
  
  /**
   * Builds the index structure and returns an index instance.
   * DataOutput(db, f + 'z') looks like:
   * s int size
   * l, p : l int, length of token, p pointer on token entry in datafile
   * ...
   * DataOutput(db, f + 'x') looks like;
   * t0, t2, ..., tl, s, pre0, pre1, ... pres, pos0, ... poss
   * t0 - tl are the token (byte[]), s size (int) of pre values (int)  
   * 
   * @param data data reference
   * @return index instance
   * @throws IOException IO Exception
   */
  public Fuzzy build(final Data data) throws IOException {
    // object[0] = number of entries in object
    // object[i] = all tokens (sorted) with length i
    // object[i] = {[t0,t1,...,ti, n, pre0, ..., pren, pos0, ..., posn], ...}
    
    Object[] o = new Object[128];
    o[0] = 0;
    o = cta.doPreOrderTravWISS(0, new StringBuffer(), o);
    
    final String db = data.meta.dbname;
    final String f = DATATXT;
    
    DataOutput out = new DataOutput(db, f + 'z');
    DataOutput outs = new DataOutput(db, f + 'x');
    
    // write index size
    outs.writeInt(((Integer) o[0]).intValue() + 1);
    
    int ce = 0;
    int bw = 0;
    
    for (int in = 1; in < o.length; in++) {
      if (o[in] != null) {
        int[][] td = (int[][]) o[in];
        // write tokenlength
        outs.write((byte) in);
        // write pointer on ftdata
        outs.writeInt(bw);
        
        for (int k = 0; k < td.length; k++) {
          // write token as byte
          for (int j = 0; j < in; j++) {
            out.write((byte) td[k][j]);
          }
          // write datasize as int
          out.writeInt(td[k][in]);
          // write pre/pos values
          for (int j = in + 1; j < td[k].length; j++) {
            out.writeInt(td[k][j]);
          }
        }
        
        bw = out.size();
        ce++;
        if (ce == (Integer) o[0]) {
          // write tokenlength
          outs.write((byte) (o.length - 1));
          // write pointer on dat
          outs.writeInt(bw); 
          break;
        }
      }
    } 
    outs.close();
    out.close();
    
    // 
    return new Fuzzy(data, db);
  }
  
  /**
   * Build a fuzzy structure, using an index with the first chars of a token.
   * @param data diskdata
   * @return index
   * @throws IOException IOException
   */
  public Fuzzy buildWS(final Data data) throws IOException {
    IntArrayList index  = new IntArrayList();
    // ftdata :
    // {[L, NB, t1, t2, ..., #pre, pre1, pre2, ..., preN, pos1, pos2, ..., posN]
    //  , ...}
    // bl: {[v, s], ...}

    ftdata = cta.doPreOrderTravWI(0, new StringBuffer(), 
        new IntArrayList(true), index);
    Object[] o = new Object[128];
    o[0] = 0;
    o = cta.doPreOrderTravWISS(0, new StringBuffer(), o);
    
    index.add(new int[]{Integer.MAX_VALUE, ftdata.length - 1});
    index.finish();
    
    final String db = data.meta.dbname;
    final String f = DATATXT;
    
    DataOutput out = new DataOutput(db, f + 'z');
    DataOutput outs = new DataOutput(db, f + 'x');
    
    // [v, pointer]
    int[][] ind = index.finish();
    // write index size
    outs.writeInt(ind.length);
    System.out.println("indessize=" + ind.length);
    System.out.println("Indexvalues:");
    // write index to file: {[v, pointer], ...}
    for (int[] in : ind) {
      System.out.println((byte) in[0] + "," + in[1]);
      outs.write((byte) in[0]); // value v (first char)
      outs.writeInt(in[1]); // pointer
    }
    
    // write ftdata size
    outs.writeInt(ftdata.length);
    System.out.println("ftdatasize=" + ftdata.length);
    // write index on ftdata and ftdata
    for(i = 0; i < ftdata.length; i++) {
      System.out.print("ts=" + ftdata[i][0]);
      // write token size
      outs.write((byte) ftdata[i][0]);
      System.out.print(";nb=" + ftdata[i][1]);
      // write pointer on token
      outs.writeInt(ftdata[i][1]);
      
      //System.out.println((byte)ftdata[i][0] + "," + ftdata[i][1]);
     
      byte[] t = new byte[ftdata[i][0]];
      // write token as byte
      for (int j = 0; j < ftdata[i][0]; j++) {
        t[j] = (byte) ftdata[i][j + 2];
        out.write((byte) ftdata[i][j + 2]);
      }
      
      // write number pre values
      //out.writeInt(ftdata[i][ftdata[i][0]+2]);
      System.out.print("\"" + new String(t) 
      + "\";s=" + ftdata[i][ftdata[i][0] + 2] + ";");
      // write ftdata as int, including number of pre values as first value
      for (int j = ftdata[i][0] + 2; j < ftdata[i].length; j++) {
        out.writeInt(ftdata[i][j]);
        System.out.print(ftdata[i][j] + ",");
      }
      System.out.println();
    }
    outs.close();
    out.close();
    
    // 
    return new Fuzzy(data, db);
  }
  
  
  @Override
  public String tit() {
    return PROGINDEX;
  }

  @Override
  public String det() {
    return INDEXTXT;
  }

  @Override
  public double prog() {
    return (double) i / ftdata.length;
  }
}

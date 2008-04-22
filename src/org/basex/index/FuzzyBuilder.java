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
  /** Current parsing value. */
  private int id;
  /** Current parsing value. */
  private int total;
  /** Temporary token reference. */
  private byte[] tmptok;
  /** Temporary token start. */
  private int tmps;
  /** Temporary token end. */
  private int tmpe;
  /** Temporary token length. */
  private int tmpl;
  /** Tokens, saved temp. in building process. **/
  public IntArrayList[] token;
  /** DataOuput for token index information. */
  private DataOutput outi;
  /** DataOuput for the tokens. */
  private DataOutput outt;
  /** DataOuput for the ftdata of a token. */
  private DataOutput outd;
  /** Temp. space for ftdata information. */
  private int[] ftd;
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
   *                each tokenlength [byte, int]
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
    token = new IntArrayList[128];
    token[0] = new IntArrayList();
    token[0].add(new int[]{0});
    ftpre = new IntArrayList[128];
    ftpre[0] = new IntArrayList();
    ftpre[0].add(new int[]{0});
    ftpos = new IntArrayList[128];
    ftpos[0] = new IntArrayList();
    ftpos[0].add(new int[]{0});   

    
    
    total = data.size;
    for(id = 0; id < total; id++) {
      checkStop(); //if(stopped()) throw new IOException(CANCELCREATE);
      //if(data.kind(id) == Data.TEXT) index(data.text(id), id, false, null);
      if(data.kind(id) == Data.TEXT) index(data.text(id), id); //, false, null);
    }
       
    int isize = token[0].list[0][0];
    final String db = data.meta.dbname;
    final String f = DATAFTX;
    
    outi = new DataOutput(db, f + 'x');
    outt = new DataOutput(db, f + 'y');
    outd = new DataOutput(db, f + 'z');
 
    // write index size
    outi.write((byte) (isize + 1));
    int[][] ind = new int[isize + 1][2];
    int[] dtmp;
    int c = 0, tr = 0, dr = 0, ct = 0, j = 1;
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
          outt.writeInt(token[j].list[t][token[j].list[t].length - 1]);
          // write data
          dtmp = new int[token[j].list[t][token[j].list[t].length - 1]];
          System.arraycopy(ftpre[j].list[t], 0, dtmp, 0, dtmp.length);
          outd.writeInts(dtmp); // pre values
          System.arraycopy(ftpos[j].list[t], 0, dtmp, 0, dtmp.length);
          outd.writeInts(dtmp); // pos values
          dr += 2 * 4L * token[j].list[t][token[j].list[t].length - 1];
          tr += ind[c][0] * 1L + 4L + 4L;
          ct++;
          t++;
        }
        c++;
      }
    }
    
    outi.write((byte) (j - 1));
    outi.writeInt((int) (tr - (j - 1) * 1L - 4L - 4L));
    ind[c][0] = j - 1;
    ind[c][1] = (int) (tr - (j - 1) * 1L - 4L - 4L);            

    if (Prop.debug) {
      System.out.println("Token Index, Tokens und FTData " +
         "im Hauptspeicher:");
      Performance.gc(5);
      System.out.println(Performance.getMem());
    }
    
    token = null;
    outi.close();
    outt.close();
    outd.close();
    ftd = new int[dr / 4];

    if (Prop.debug) {
      System.out.println("Platz für ftdata in Hauptspeicher alokiert.");
      Performance.gc(5);
      System.out.println(Performance.getMem());
    }
    
    Fuzzy index = new Fuzzy(data.meta.dbname);
    
    if (Prop.debug) {
      System.out.println("FTData im Hauptspeicher:");
      Performance.gc(5);
      System.out.println(Performance.getMem());
    }
/*
    ind = null;

    for(id = 0; id < total; id++) {
      if(stopped()) throw new IOException(CANCELCREATE);
      if(data.kind(id) == Data.TEXT) index(data.text(id), id, true, index);
    }
    
    // write ftdata
    outd.writeInts(ftd);
    outd.close();
    ftd = null;
    */
    index.openDataFile(data.meta.dbname);
    return index;
  }
  
  /**
   * Extracts and indexes words from the specified byte array.
   * @param tok token to be extracted and indexed
   * @param pre int pre value
   */
  private void index(final byte[] tok, final int pre) { //, 
      //final boolean addData, final Fuzzy index) {
    tmptok = tok;
    tmpe = -1;
    tmpl = tok.length;
    while(parse()) //index(pre, addData, index);
      index(pre);
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


  /**
   * Indexes a single token.
   * @param pre pre value
   */
//private void index(final int pre, final boolean addData, final Fuzzy index) {
  private void index(final int pre) {
    if(tmpe - tmps > Token.MAXLEN) return;

    final byte[] tok = new byte[tmpe - tmps];
    for(int t = 0; t < tok.length; t++) {
      tok[t] = (byte) Token.ftNorm(tmptok[tmps + t]);
    }
    
    index(tok, pre, tmps);
    //if (addData) indexData(tok, pre, tmps, index);
    //else index(tok);

    /**cont = Num.add(cont, tok);
    BaseX.debug("cont:" + Token.toString(cont));
    **/
  }
 
  /**
   * Indexes a single token.
   * @param tok token to be indexed
   * @param pre pre value of the token
   * @param pos position value of the token
   */
  public void index(final byte[] tok, final int pre, final int pos) {
    if (token[tok.length] == null) {
      token[0].list[0][0]++;
      IntArrayList ial = new IntArrayList();
      int[] itok = new int[tok.length + 1];
      for (int t = 0; t < tok.length; t++) itok[t] = tok[t];
      itok[tok.length] = 1;
      ial.add(itok);
      token[tok.length] = ial;
      ftpre[tok.length] = new IntArrayList();
      ftpre[tok.length].add(new int[]{pre});
      ftpos[tok.length] = new IntArrayList();
      ftpos[tok.length].add(new int[]{pos});
    } else {
      int[] itok = new int[tok.length];
      for (int t = 0; t < tok.length; t++) itok[t] = tok[t];
      int m = token[tok.length].addSorted(itok, itok.length);
      if (token[tok.length].found) {
        token[tok.length].list[m][tok.length]++;
        int n = 
          token[tok.length].list[m][token[tok.length].list[m].length - 1] - 1;
        if (n == ftpre[tok.length].list[m].length) {
          int[] tmp = Array.resize(ftpre[tok.length].list[m], 
              ftpre[tok.length].list[m].length, 
              ftpos[tok.length].list[m].length << 1);
          //tmp[tmp.length - 1] = tmp[ftpre[tok.length].list[m].length - 1] + 1;
          tmp[n] = pre;
          ftpre[tok.length].list[m] = tmp;
          
          tmp = Array.resize(ftpos[tok.length].list[m], 
              ftpos[tok.length].list[m].length, 
              ftpos[tok.length].list[m].length << 1);
          tmp[n] = pos;
          ftpos[tok.length].list[m] = tmp;
        } else {
          ftpre[tok.length].list[m] [n] = pre;
          ftpos[tok.length].list[m] [n] = pos;
         // ftpre[tok.length].list[m] [ftpre[tok.length].list[m].length - 1]++; 
        }
      } else {
        ftpre[tok.length].addAt(new int[]{pre}, m);
        ftpos[tok.length].addAt(new int[]{pos}, m);          
      }
    }
  }

  

  
  /**
   * Indexes a single token.
   * @param tok token to be indexed
   */
  public void index(final byte[] tok) {
    if (token[tok.length] == null) {
      token[0].list[0][0]++;
      IntArrayList ial = new IntArrayList();
      int[] itok = new int[tok.length + 1];
      for (int t = 0; t < tok.length; t++) itok[t] = tok[t];
      itok[tok.length] = 1;
      ial.add(itok);
      token[tok.length] = ial;
    } else {
      int[] itok = new int[tok.length];
      for (int t = 0; t < tok.length; t++) itok[t] = tok[t];
      int m = token[tok.length].addSorted(itok, itok.length);
      if (token[tok.length].found) 
        token[tok.length].list[m][tok.length]++;
    }
  }
  
  /**
   * Index ftdata to an existing token.
   * 
   * @param tok token where to add the ftdata
   * @param pre pre value to add
   * @param pos position value to add
   * @param index index, where to add the data
   */
  public void indexData(final byte[] tok, final int pre, final int pos, 
      final Fuzzy index) {
    int p = index.getPointerOnToken(tok);
    
    if(p != -1) {
      int pd = index.getPointerOnData(p, tok.length);
      int sd = index.getDataSize(p, tok.length);
      // read how many values are written
      int r = ftd[pd / 4 + sd - 1];
      // write pre value
      ftd[pd / 4 + r] = pre;
      // write pos value
      ftd[pd / 4 + sd + r] = pos;
      if (r < sd - 1) ftd[pd / 4 + sd - 1]++;
    }
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
    return new Fuzzy(db);
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
    return 1; //return (double) i / ftdata.length;
  }
}

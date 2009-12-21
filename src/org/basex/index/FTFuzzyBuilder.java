package org.basex.index;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.File;
import java.io.IOException;

import org.basex.core.proc.DropDB;
import org.basex.data.Data;
import org.basex.io.DataOutput;
import org.basex.io.IO;
import org.basex.util.IntList;
import org.basex.util.Num;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;

/**
 * This class builds an index for text contents, optimized for fuzzy search,
 * in an ordered table.
 *
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
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Sebastian Gath
 * @author Christian Gruen
 */
public final class FTFuzzyBuilder extends FTBuilder {
  /** Word parser. */
  private ValueFTTrees tree = new ValueFTTrees();
  /** Number of indexed tokens. */
  private long ntok;
  /** Runtime for memory consumption. */
  private final Runtime rt;
  /** Maximal memory. */
  final long maxMem;
  /** Current file id. */
  int cf = 0;
  
  /**
   * Constructor.
   * @param d data reference
   * @throws IOException IOException
   */
  protected FTFuzzyBuilder(final Data d) throws IOException {
    super(d);
    rt = Runtime.getRuntime();
    Performance.gc(2);
    maxMem = (long) (rt.maxMemory() * 0.8);
  }

  @Override
  public FTIndex build() throws IOException {
    index();
    return new FTFuzzy(data);
  }

  @Override
  void index(final byte[] tok) throws IOException{
    // currently no frequency support for tfidf based scoring
    if((ntok & 0xFFFF) == 0 && scm == 0 &&
        rt.totalMemory() - rt.freeMemory() > maxMem) {
      writeIndex();
      Performance.gc(2);
      cf++;      
    }    
    tree.index(tok, pre, wp.pos, cf);
    ntok++;
  }

  @Override
  int nrTokens() {
    int l = 0;
    for (int i = 0; i < tree.sizes.size(); i++) { 
      final ValueFTTree t = tree.trees[i];
      l += t.size();
    }
    return l;
  }

  @Override
  void getFreq() {
    tree.init();
    while(tree.more((byte) 0)) {
      tree.nextPoi();
      getFreq(tree.nextPres());
    }
  }

  /**
   * Rename single index files.
   */
  public void single() {
    File f = data.meta.file(DATAFTX + 0 + 'x');
    f.renameTo(data.meta.file(DATAFTX + 'x').getAbsoluteFile());
    f = data.meta.file(DATAFTX + 0 + 'y');
    f.renameTo(data.meta.file(DATAFTX + 'y').getAbsoluteFile());
    f = data.meta.file(DATAFTX + 0 + 'z');
    f.renameTo(data.meta.file(DATAFTX + 'z').getAbsoluteFile());
  }
  
  @Override
  public void write() throws IOException {
    writeIndex();
    if (cf == 0) {      
      single();
      return;
    }
    
    cf++;

    final DataOutput outx = new DataOutput(data.meta.file(DATAFTX + 'x'));
    final DataOutput outy = new DataOutput(data.meta.file(DATAFTX + 'y'));
    final DataOutput outz = new DataOutput(data.meta.file(DATAFTX + 'z'));
    
    final byte[][] tok = new byte[cf][];
    final int[][] pres = new int[cf][];
    final int[][] pos = new int[cf][];
    final IntList ind = new IntList();
    
    final FTFuzzy[] v = new FTFuzzy[cf];
    for (byte b = 0; b < cf; b++) {
      v[b] = new FTFuzzy(data, b);
      tok[b] = v[b].nextTok();
      pres[b] = v[b].nextPreValues();
      pos[b] = v[b].nextPosValues();
    }
    
    int min;
    final IntList mer = new IntList();
    while(check(tok)) {
      min = 0;
      mer.reset();
      mer.add(min);
      for(int i = 0; i < cf; i++) {
        if(min == i || tok[i].length == 0) continue;
        final int l = tok[i].length - tok[min].length;
        final int d = diff(tok[min], tok[i]);
        if(l < 0 || l == 0 && d > 0 || tok[min].length == 0) {
          min = i;
          mer.reset();
          mer.add(min);
        } else if(d == 0 && tok[i].length > 0) {
//          if(merge.size() == 0) merge.add(min);
          mer.add(i);
        }
      }
      
      if (ind.size() == 0 || ind.get(ind.size() - 2) < tok[min].length) {
        ind.add(tok[min].length);
        ind.add((int) outy.size());
      } 

      outy.write(tok[min]);
      outy.write5(outz.size());
      int s = 0;
      final TokenBuilder tbp = new TokenBuilder();
      final TokenBuilder tbo = new TokenBuilder();
      tbp.add(new byte[4]);
      tbo.add(new byte[4]);
      for(int j = 0; j < mer.size(); j++) {
        final int m = mer.get(j);
        for (int p : pres[m]) tbp.add(Num.num(p));
        for (int p : pos[m]) tbo.add(Num.num(p));
        s += v[m].nextFTDataSize();
        tok[m] = nextToken(v, m);
        pres[m] = tok[m].length > 0 ? v[m].nextPreValues() : new int[]{};
        pos[m] = tok[m].length > 0 ? v[m].nextPosValues() : new int[]{};
      }

      outy.writeInt(s);
      // write compressed pre and pos arrays
      final byte[] p = tbp.finish();      
      Num.size(p, p.length);
      final byte[] o = tbo.finish();
      Num.size(o, o.length);
      writeFTData(outz, p, o);
    }
    writeInd(outx, ind, ind.get(ind.size() - 2) + 1, (int) outy.size());
    
    outx.close();
    outy.close();
    outz.close();
    DropDB.delete(data.meta.name, DATAFTX + "\\d+." + IO.BASEXSUFFIX,
        data.meta.prop);
  }
  
  /**
   * Retuns next token.
   * @param v FTFuzzy Array
   * @param m pointer on current FTFuzzy
   * @return next token
   * @throws IOException
   */
  private byte[] nextToken(final FTFuzzy[] v, final int m) throws IOException {
    if (v[m] == null) return EMPTY;
    final byte[] tok = v[m].nextTok();    
    if (tok.length > 0) return tok;
    v[m].close();
    v[m] = null;
    return EMPTY;    
  }
  
  /**
   * Write tokenlength index to disk.
   * @param outx Output
   * @param ind IntList with token lenght and offset
   * @param ls last token length 
   * @param lp last offset
   * @throws IOException
   */
  private void writeInd(final DataOutput outx, final IntList ind, 
      final int ls, final int lp) throws IOException {
    outx.write(ind.size() / 2);
    for (int i = 0; i < ind.size(); i+=2) {
      outx.write(ind.get(i));
      outx.writeInt(ind.get(i + 1));
    }
    outx.write(ls);
    outx.writeInt(lp);

  }
  
  /**
   * Checks if any unprocessed pre values are remaining.
   * @param tok byte[][] tokens
   * @return boolean
   */
  private boolean check(final byte[][] tok) {
    for(final byte[] b : tok) if (b.length > 0) return true;
    return false;
  }

  /**
   * Write index to disk.
   * @throws IOException
   */
  void writeIndex() throws IOException {
    tree.init();
    
    final DataOutput outx = new DataOutput(data.meta.file(DATAFTX + cf + 'x'));
    final DataOutput outy = new DataOutput(data.meta.file(DATAFTX + cf + 'y'));
    final DataOutput outz = new DataOutput(data.meta.file(DATAFTX + cf + 'z'));

    final IntList ind = new IntList();
    long dr = 0;
    int tr = 0;
    byte j = 0;
    int c = 0; 
    while(tree.more(cf)) {
      c++;
      tree.nextPoi();
      final byte[] key = tree.nextTok();      
      if (j < key.length) {
        j = (byte) key.length;
        // write index and pointer on first token
        ind.add(j);
        ind.add(tr);
      }
      
      for(int i = 0; i < j; i++) outy.write(key[i]);

      // write pointer on data and data size
      outy.write5(dr);
      outy.writeInt(tree.nextNumPre());
      // write compressed pre and pos arrays
      writeFTData(outz, tree.nextPres(), tree.nextPos());

      dr = outz.size();
      tr = (int) outy.size();
    }
    writeInd(outx, ind, ++j, tr);

    outx.close();
    outy.close();
    outz.close();
    tree.initTrees(); 
  }
}

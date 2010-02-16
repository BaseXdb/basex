package org.basex.index;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

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
 *  - (1) the tokens are collected in main memory (red-black tree)
 *  - (2) if main memory, the data is written to disk, (1)
 *  - (3) merge disk data into the final format:
 *
 * The building process is divided in 2 steps:
 * a)
 *    fill DataOutput(db, f + 'x') looks like:
 *    [l, p] ...
 *      - l is the length [byte] of a token
 *      - p the pointer [int] of the first token with length l
 *      there's an entry for each token length
 *
 *    fill DataOutput(db, f + 'y') looks like:
 *    [t0, t1, ... tl, z, s]
 *      - t0, t1, ... tl-1 is the token [byte[l]]
 *      - z is the pointer on the data entries of the token [long]
 *      - s is the number of pre values, saved in data [int]
 * b)
 *    fill DataOutput(db, f + 'z') looks like: stores the full text data;
 *      the pre values are ordered but not distinct
 *      [pre1, pos1, pre2, pos2, pre3, pos3, ...] as Nums
 *
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Sebastian Gath
 * @author Christian Gruen
 */
public final class FTFuzzyBuilder extends FTBuilder {
  /** Word parser. */
  private final ValueFTTrees tree = new ValueFTTrees();

  /**
   * Constructor.
   * @param d data reference
   * @throws IOException IOException
   */
  protected FTFuzzyBuilder(final Data d) throws IOException {
    super(d);
  }

  @Override
  public FTIndex build() throws IOException {
    index();
    return new FTFuzzy(data);
  }

  @Override
  void index(final byte[] tok) throws IOException {
    // until there's enough free main memory
    if((ntok & 0xFFF) == 0 && scm == 0 && memFull()) {
      // currently no frequency support for tfidf based scoring
      writeIndex(csize++);
      Performance.gc(2);
    }
    tree.index(tok, pre, wp.pos, csize);
    ntok++;
  }

  @Override
  int nrTokens() {
    int l = 0;
    for(final ValueFTTree tree2 : tree.trees) {
      if(tree2 != null) l += tree2.size();
    }
    return l;
  }

  @Override
  void getFreq() {
    tree.init();
    while(tree.more(0)) {
      final ValueFTTree t = tree.nextTree();
      t.next();
      getFreq(t.nextPres());
    }
  }

  @Override
  public void write() throws IOException {
    writeIndex(csize++);
    if(!merge) return;

    // merges temporarily indexes to the final index
    final DataOutput outx = new DataOutput(data.meta.file(DATAFTX + 'x'));
    final DataOutput outy = new DataOutput(data.meta.file(DATAFTX + 'y'));
    final DataOutput outz = new DataOutput(data.meta.file(DATAFTX + 'z'));

    final byte[][] tok = new byte[csize][];
    final int[][] pres = new int[csize][];
    final int[][] pos = new int[csize][];
    final IntList ind = new IntList();

    // open all temp indexes
    final FTFuzzy[] v = new FTFuzzy[csize];
    for(int b = 0; b < csize; b++) {
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
      // find next token to write on disk
      for(int i = 0; i < csize; i++) {
        if(min == i || tok[i].length == 0) continue;
        final int l = tok[i].length - tok[min].length;
        final int d = diff(tok[min], tok[i]);
        if(l < 0 || l == 0 && d > 0 || tok[min].length == 0) {
          min = i;
          mer.reset();
          mer.add(min);
        } else if(d == 0 && tok[i].length > 0) {
          mer.add(i);
        }
      }

      if(ind.size() == 0 || ind.get(ind.size() - 2) < tok[min].length) {
        ind.add(tok[min].length);
        ind.add((int) outy.size());
      }

      // write token
      outy.write(tok[min]);
      // pointer on full text data
      outy.write5(outz.size());
      int s = 0;
      final TokenBuilder tbp = new TokenBuilder();
      final TokenBuilder tbo = new TokenBuilder();
      tbp.add(new byte[4]);
      tbo.add(new byte[4]);
      // merge full text data of all temp indexes with the same token
      for(int j = 0; j < mer.size(); j++) {
        final int m = mer.get(j);
        for(final int p : pres[m]) tbp.add(Num.num(p));
        for(final int p : pos[m]) tbo.add(Num.num(p));
        s += v[m].nextFTDataSize();
        tok[m] = nextToken(v, m);
        pres[m] = tok[m].length > 0 ? v[m].nextPreValues() : new int[0];
        pos[m] = tok[m].length > 0 ? v[m].nextPosValues() : new int[0];
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
   * Writes the token length index to disk.
   * @param outx Output
   * @param ind IntList with token length and offset
   * @param ls last token length
   * @param lp last offset
   * @throws IOException I/O exception
   */
  private void writeInd(final DataOutput outx, final IntList ind,
      final int ls, final int lp) throws IOException {
    outx.write(ind.size() >> 1);
    for(int i = 0; i < ind.size(); i += 2) {
      outx.write(ind.get(i));
      outx.writeInt(ind.get(i + 1));
    }
    outx.write(ls);
    outx.writeInt(lp);
  }

  /**
   * Returns next token.
   * @param v FTFuzzy Array
   * @param m pointer on current FTFuzzy
   * @return next token
   * @throws IOException I/O exception
   */
  protected byte[] nextToken(final FTFuzzy[] v, final int m)
      throws IOException {

    if(v[m] == null) return EMPTY;
    final byte[] tok = v[m].nextTok();
    if(tok.length > 0) return tok;
    v[m].close();
    v[m] = null;
    return EMPTY;
  }

  /**
   * Writes the main memory index to disk - temporarily.
   * @param cs current file pointer
   * @throws IOException I/O exception
   */
  void writeIndex(final int cs) throws IOException {
    final String s = DATAFTX + (merge ? Integer.toString(cs) : "");
    final DataOutput outx = new DataOutput(data.meta.file(s + 'x'));
    final DataOutput outy = new DataOutput(data.meta.file(s + 'y'));
    final DataOutput outz = new DataOutput(data.meta.file(s + 'z'));

    final IntList ind = new IntList();
    long dr = 0;
    int tr = 0;
    int j = 0;
    tree.init();
    while(tree.more(cs)) {
      final ValueFTTree t = tree.nextTree();
      t.next();
      final byte[] key = t.nextTok();

      if(j < key.length) {
        j = key.length;
        // write index and pointer on first token
        ind.add(j);
        ind.add(tr);
      }
      for(int i = 0; i < j; i++) outy.write(key[i]);
      // write pointer on full text data
      outy.write5(dr);
      // write full text data size (number of pre values)
      outy.writeInt(t.nextNumPre());
      // write compressed pre and pos arrays
      writeFTData(outz, t.nextPres(), t.nextPos());

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

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
  private final ValueFTTrees tree = new ValueFTTrees();
  /** Number of indexed tokens. */
  private long ntok;
  /** Number of cached index structures. */
  private int csize;

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

    final DataOutput outx = new DataOutput(data.meta.file(DATAFTX + 'x'));
    final DataOutput outy = new DataOutput(data.meta.file(DATAFTX + 'y'));
    final DataOutput outz = new DataOutput(data.meta.file(DATAFTX + 'z'));

    final byte[][] tok = new byte[csize][];
    final int[][] pres = new int[csize][];
    final int[][] pos = new int[csize][];
    final IntList ind = new IntList();

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

      outy.write(tok[min]);
      outy.write5(outz.size());
      int s = 0;
      final TokenBuilder tbp = new TokenBuilder();
      final TokenBuilder tbo = new TokenBuilder();
      tbp.add(new byte[4]);
      tbo.add(new byte[4]);
      for(int j = 0; j < mer.size(); j++) {
        final int m = mer.get(j);
        for(final int p : pres[m]) tbp.add(Num.num(p));
        for(final int p : pos[m]) tbo.add(Num.num(p));
        s += v[m].nextFTDataSize();
        tok[m] = nextToken(v, m);
        pres[m] = tok[m].length > 0 ? v[m].nextPreValues() : new int[] {};
        pos[m] = tok[m].length > 0 ? v[m].nextPosValues() : new int[] {};
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
   * Returns next token.
   * @param v FTFuzzy Array
   * @param m pointer on current FTFuzzy
   * @return next token
   * @throws IOException I/O exception
   */
  private byte[] nextToken(final FTFuzzy[] v, final int m) throws IOException {
    if(v[m] == null) return EMPTY;
    final byte[] tok = v[m].nextTok();
    if(tok.length > 0) return tok;
    v[m].close();
    v[m] = null;
    return EMPTY;
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
   * Checks if any unprocessed pre values are remaining.
   * @param tok byte[][] tokens
   * @return boolean
   */
  private boolean check(final byte[][] tok) {
    for(final byte[] b : tok) if(b.length > 0) return true;
    return false;
  }

  /**
   * Writes the index to disk.
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
      // write pointer on data and data size
      outy.write5(dr);
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

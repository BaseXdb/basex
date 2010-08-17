package org.basex.index;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Data;
import org.basex.io.DataOutput;
import org.basex.util.IntList;

/**
 * This class builds an index for text contents, optimized for fuzzy search,
 * in an ordered table.
 *
 *  - (1) the tokens are collected in main-memory (red-black tree)
 *  - (2) if main-memory, the data is written to disk, (1)
 *  - (3) merge disk data into the final format:
 *
 * The building process is divided in two steps:
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
 *    fill DataOutput(db, f + 'z') looks like: stores the full-text data;
 *      the pre values are ordered but not distinct
 *      [pre1, pos1, pre2, pos2, pre3, pos3, ...] as Nums
 *
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Sebastian Gath
 * @author Christian Gruen
 */
final class FTFuzzyBuilder extends FTBuilder {
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
  void index(final byte[] tok) {
    tree.index(tok, pre, wp.pos, csize);
  }

  @Override
  int nrTokens() {
    int l = 0;
    for(final ValueFTTree t : tree.trees) if(t != null) l += t.size();
    return l;
  }

  @Override
  void calcFreq() {
    tree.init();
    while(tree.more(0)) {
      final ValueFTTree t = tree.nextTree();
      t.next();
      calcFreq(t.nextPres());
    }
  }

  @Override
  public void write() throws IOException {
    writeIndex(csize++);
    if(!merge) return;

    // merges temporary index files
    final DataOutput outx = new DataOutput(data.meta.file(DATAFTX + 'x'));
    final DataOutput outy = new DataOutput(data.meta.file(DATAFTX + 'y'));
    final DataOutput outz = new DataOutput(data.meta.file(DATAFTX + 'z'));
    final IntList ind = new IntList();

    // open all temporary sorted lists
    final FTList[] v = new FTList[csize];
    for(int b = 0; b < csize; ++b) v[b] = new FTFuzzyList(data, b);

    final IntList il = new IntList();
    while(check(v)) {
      int min = 0;
      il.reset();
      il.add(min);
      // find next token to write on disk
      for(int i = 0; i < csize; ++i) {
        if(min == i || v[i].tok.length == 0) continue;
        final int l = v[i].tok.length - v[min].tok.length;
        final int d = diff(v[min].tok, v[i].tok);
        if(l < 0 || l == 0 && d > 0 || v[min].tok.length == 0) {
          min = i;
          il.reset();
          il.add(min);
        } else if(d == 0 && v[i].tok.length > 0) {
          il.add(i);
        }
      }

      if(ind.size() == 0 || ind.get(ind.size() - 2) < v[min].tok.length) {
        ind.add(v[min].tok.length);
        ind.add((int) outy.size());
      }

      // write token
      outy.writeBytes(v[min].tok);
      // pointer on full-text data
      outy.write5(outz.size());

      // merge and write out data size
      final int s = merge(outz, il, v);
      outy.write4(s);
    }
    writeInd(outx, ind, ind.get(ind.size() - 2) + 1, (int) outy.size());

    outx.close();
    outy.close();
    outz.close();
  }

  /**
   * Writes the token length index to disk.
   * @param outx output
   * @param il token length and offsets
   * @param ls last token length
   * @param lp last offset
   * @throws IOException I/O exception
   */
  private void writeInd(final DataOutput outx, final IntList il,
      final int ls, final int lp) throws IOException {

    final int is = il.size();
    outx.write1(is >> 1);
    for(int i = 0; i < is; i += 2) {
      outx.write1(il.get(i));
      outx.write4(il.get(i + 1));
    }
    outx.write1(ls);
    outx.write4(lp);
  }

  @Override
  protected void writeIndex(final int cs) throws IOException {
    final String s = DATAFTX + (merge ? cs : "");
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
      for(int i = 0; i < j; ++i) outy.write1(key[i]);
      // write pointer on full-text data
      outy.write5(dr);
      // write full-text data size (number of pre values)
      outy.write4(t.nextNumPre());
      // write compressed pre and pos arrays
      writeFTData(outz, t.nextPres(), t.nextPoss());

      dr = outz.size();
      tr = (int) outy.size();
    }
    writeInd(outx, ind, ++j, tr);

    outx.close();
    outy.close();
    outz.close();
    tree.initFT();
  }
}

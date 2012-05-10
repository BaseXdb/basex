package org.basex.index.ft;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.data.*;
import org.basex.io.out.DataOutput;
import org.basex.util.list.*;

/**
 * <p>This class builds an index for text contents, optimized for fuzzy search,
 * in an ordered table:</p>
 *
 * <ol>
 * <li> The tokens are indexed in a main memory tree structure.</li>
 * <li> If main memory is full, the index is written to disk.</li>
 * <li> The temporary index instances are merged.</li>
 * </ol>
 *
 * <p>The file format is described in the {@link FTFuzzy} class.</p>
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Sebastian Gath
 * @author Christian Gruen
 */
public final class FTFuzzyBuilder extends FTBuilder {
  /** Value trees. */
  private final FTIndexTrees tree;

  /**
   * Constructor.
   * @param d data reference
   * @throws IOException IOException
   */
  public FTFuzzyBuilder(final Data d) throws IOException {
    super(d);
    tree = new FTIndexTrees(d.meta.maxlen);
  }

  @Override
  public FTIndex build() throws IOException {
    index();
    return new FTFuzzy(data);
  }

  @Override
  void index(final byte[] tok) {
    tree.index(tok, pre, pos, csize);
  }

  @Override
  int nrTokens() {
    int l = 0;
    for(final FTIndexTree t : tree.trees) if(t != null) l += t.size();
    return l;
  }

  @Override
  void calcFreq() {
    tree.init();
    while(tree.more(0)) {
      final FTIndexTree t = tree.nextTree();
      t.next();
      calcFreq(t.nextPres());
    }
  }

  @Override
  public void write() throws IOException {
    writeIndex(csize++);
    if(!merge) return;

    // merges temporary index files
    final DataOutput outX = new DataOutput(data.meta.dbfile(DATAFTX + 'x'));
    final DataOutput outY = new DataOutput(data.meta.dbfile(DATAFTX + 'y'));
    final DataOutput outZ = new DataOutput(data.meta.dbfile(DATAFTX + 'z'));
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

      if(ind.isEmpty() || ind.get(ind.size() - 2) < v[min].tok.length) {
        ind.add(v[min].tok.length);
        ind.add((int) outY.size());
      }

      // write token
      outY.writeBytes(v[min].tok);
      // pointer on full-text data
      outY.write5(outZ.size());
      // merge and write data size
      outY.write4(merge(outZ, il, v));
    }
    writeInd(outX, ind, ind.get(ind.size() - 2) + 1, (int) outY.size());

    outX.close();
    outY.close();
    outZ.close();
  }

  /**
   * Writes the token length index to disk.
   * @param outX output
   * @param il token length and offsets
   * @param ls last token length
   * @param lp last offset
   * @throws IOException I/O exception
   */
  private static void writeInd(final DataOutput outX, final IntList il,
      final int ls, final int lp) throws IOException {

    final int is = il.size();
    outX.writeNum(is >> 1);
    for(int i = 0; i < is; i += 2) {
      outX.writeNum(il.get(i));
      outX.write4(il.get(i + 1));
    }
    outX.writeNum(ls);
    outX.write4(lp);
  }

  @Override
  protected void writeIndex(final int cs) throws IOException {
    final String s = DATAFTX + (merge ? cs : "");
    final DataOutput outX = new DataOutput(data.meta.dbfile(s + 'x'));
    final DataOutput outY = new DataOutput(data.meta.dbfile(s + 'y'));
    final DataOutput outZ = new DataOutput(data.meta.dbfile(s + 'z'));

    final IntList ind = new IntList();
    long dr = 0;
    int tr = 0;
    int j = 0;
    tree.init();
    while(tree.more(cs)) {
      final FTIndexTree t = tree.nextTree();
      t.next();
      final byte[] key = t.nextTok();

      if(j < key.length) {
        j = key.length;
        // write index and pointer on first token
        ind.add(j);
        ind.add(tr);
      }
      for(int i = 0; i < j; ++i) outY.write1(key[i]);
      // write pointer on full-text data
      outY.write5(dr);
      // write full-text data size (number of pre values)
      outY.write4(t.nextNumPre());
      // write compressed pre and pos arrays
      writeFTData(outZ, t.nextPres(), t.nextPoss());

      dr = outZ.size();
      tr = (int) outY.size();
    }
    writeInd(outX, ind, ++j, tr);

    outX.close();
    outY.close();
    outZ.close();
    tree.initFT();
  }
}

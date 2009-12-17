package org.basex.index;

import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.data.Data;
import org.basex.io.DataOutput;
import org.basex.util.Performance;
import org.basex.util.Token;

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
  private FTHash[] tree = new FTHash[Token.MAXLEN + 1];
  /** Number of unique word lengths. */
  private byte isize = 1;
  /** Number of indexed tokens. */
  private int ntok;

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
    Performance.gc(4);
    System.out.println("- Start: " + Performance.getMem());
    index();
    return new FTFuzzy(data);
  }

  @Override
  void index(final byte[] tok) {
    final int tl = tok.length;
    if(tree[tl] == null) {
      isize++;
      tree[tl] = new FTHash();
    }
    tree[tl].index(tok, pre, wp.pos);
    ntok++;
  }

  @Override
  int nrTokens() {
    int l = 0;
    for(final FTHash t : tree) if(t != null) l += t.size();
    return l;
  }

  @Override
  void getFreq() {
    for(final FTHash t : tree) {
      if(t == null) continue;
      t.init();
      while(t.more()) getFreq(t.pre[t.next()]);
    }
  }

  @Override
  void write() throws IOException {
    final DataOutput outx = new DataOutput(data.meta.file(DATAFTX + 'x'));
    final DataOutput outy = new DataOutput(data.meta.file(DATAFTX + 'y'));
    final DataOutput outz = new DataOutput(data.meta.file(DATAFTX + 'z'));

    // write index size
    outx.write(isize);
    long dr = 0;
    int tr = 0;
    byte j = -1;
    while(++j < tree.length) {
      final FTHash hash = tree[j];
      if(hash == null) continue;

      // write index and pointer on first token
      outx.write(j);
      outx.writeInt(tr);

      if(scm == 0) hash.init();
      else hash.initIter();
      while(hash.more()) {
        final int p = hash.next();
        // write token value
        final byte[] key = hash.key();
        for(int i = 0; i < j; i++) outy.write(key[i]);

        // write pointer on data and data size
        outy.write5(dr);
        outy.writeInt(hash.ns[p]);
        // write compressed pre and pos arrays
        writeFTData(outz, hash.pre[p], hash.pos[p]);

        dr = outz.size();
        tr = (int) outy.size();
      }
      //tree[j] = null;
    }
    //tree = null;

    outx.write(j);
    outx.writeInt(tr);
    outx.close();
    outy.close();
    outz.close();

/*
diffMem("All", false);
for(int i = 0; i < tree.length; i++) if(tree[i] != null) tree[i].pre = null;
diffMem("Pre", true);
for(int i = 0; i < tree.length; i++) if(tree[i] != null) tree[i].pos = null;
diffMem("Pos", true);
for(int i = 0; i < tree.length; i++) if(tree[i] != null) tree[i].ns = null;
diffMem("NS", true);
for(int i = 0; i < tree.length; i++) if(tree[i] != null) tree[i].keys = null;
diffMem("Keys", true);
for(int i = 0; i < tree.length; i++) if(tree[i] != null) tree[i].next = null;
diffMem("Next", true);
for(int i = 0; i < tree.length; i++) if(tree[i] != null) tree[i].bucket = null;
diffMem("Bucket", true);
tree = null;
diffMem("Tree", true);
*/
  }

  /* Old memory consumption.
  private long oldMem;
  
  /*
   * Shows the current memory consumption/difference.
   * @param desc description
   * @param diff difference flag
  private void diffMem(final String desc, final boolean diff) {
    Performance.gc(4);
    final Runtime rt = Runtime.getRuntime();
    final long mem = rt.totalMemory() - rt.freeMemory();
    System.out.println("- " + desc + ": " +
      Performance.format(diff ? oldMem - mem : mem));
    oldMem = mem;
  }*/
}

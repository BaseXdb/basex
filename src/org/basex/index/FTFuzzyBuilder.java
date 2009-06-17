package org.basex.index;

import static org.basex.Text.*;
import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Progress;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.io.DataOutput;
import org.basex.util.Num;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.basex.util.Tokenizer;

/**
 * This class builds an index for text contents, optimized for fuzzy search,
 * in an ordered table.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Sebastian Gath
 * @author Christian Gruen
 */
public final class FTFuzzyBuilder extends Progress implements IndexBuilder {
  /** Current parsing value. */
  private int id;
  /** Current parsing value. */
  private int total;
  /** Word parser. */
  private final Tokenizer wp = new Tokenizer();
  /** Word parser. */
  private FTHash[] tree = new FTHash[Token.MAXLEN + 1];
  /** Number of indexed words. */
  private byte isize = 1;

  /**
   * Builds the index structure and returns an index instance.
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
   * @param data data reference
   * @return index instance
   * @throws IOException IO Exception
   */
  public FTFuzzy build(final Data data) throws IOException {
    final Performance p = new Performance();

    total = data.meta.size;
    for(id = 0; id < total; id++) {
      checkStop();
      if(data.kind(id) == Data.TEXT) index(data.text(id));
    }

    if(Prop.debug) {
      Performance.gc(3);
      BaseX.debug("- Indexed: % (%)", p, Performance.getMem());
    }
    write(data);

    if(Prop.debug) {
      Performance.gc(3);
      BaseX.debug("- Written: % (%)", p, Performance.getMem());
    }
    return new FTFuzzy(data);
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
   * Indexes a single token.
   */
  private void index() {
    final byte[] tok = wp.get();
    final int pos = wp.pos;
    final int tl = tok.length;
    if(tl > Token.MAXLEN) return;
    if(tree[tl] == null) {
      isize++;
      tree[tl] = new FTHash();
    }
    tree[tl].index(tok, id, pos);
  }

  /**
   * Writes the index data to disk.
   * @param data data reference
   * @throws IOException I/O exception
   */
  private void write(final Data data) throws IOException {
    final String db = data.meta.dbname;
    final DataOutput outx = new DataOutput(db, DATAFTX + 'x');
    final DataOutput outy = new DataOutput(db, DATAFTX + 'y');
    final DataOutput outz = new DataOutput(db, DATAFTX + 'z');

    // write index size
    outx.write(isize);
    long dr = 0;
    int c = 0, tr = 0;
    byte j = 1;
    for (; j < tree.length && c < isize - 1; j++) {
      final FTHash tre = tree[j];
      if(tre == null) continue;

      // write index and pointer on first token
      outx.write(j);
      outx.writeInt(tr);

      tre.init();
      while(tre.more()) {
        final int p = tre.next();

         // write token value
        final byte[] key = tre.key();
        for(int x = 0; x != j; x++) outy.write(key[x]);

        // write pointer on data
        outy.write5(dr);
        // write data size
        final int ds = tre.ns[p];
        outy.writeInt(ds);

        // write compressed pre and pos arrays
        final byte[] vpre = tre.pre[p];
        final byte[] vpos = tre.pos[p];
        int lpre = 4;
        int lpos = 4;
        // ftdata is stored here, with pre1, pos1, ..., preu, posu
        while(lpre < Num.size(vpre) && lpos < Num.size(vpos)) {
          int z = 0;
          while(z < Num.len(vpre, lpre)) outz.write(vpre[lpre + z++]);
          lpre += z;
          z = 0;
          while(z < Num.len(vpos, lpos)) outz.write(vpos[lpos + z++]);
          lpos += z;
        }

        dr = outz.size();
        tr = (int) outy.size();
      }
      c++;
    }
    tree = null;

    outx.write(j);
    outx.writeInt(tr);

    outx.close();
    outy.close();
    outz.close();
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

package org.basex.index;

import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.io.DataOutput;
import org.basex.util.Num;
import org.basex.util.ScoringTokenizer;
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
  /** Number of indexed words. */
  private byte isize = 1;

  /**
   * Constructor.
   * @param d data reference
   * @param pr database properties
   */
  public FTFuzzyBuilder(final Data d, final Prop pr) {
    super(d, pr);
  }

  @Override
  public FTIndex build() throws IOException {
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

    // [SG] INEX Budget
    if (wp instanceof ScoringTokenizer) {
      Main.outln(new String(tok) + " " + ((ScoringTokenizer) wp).score(tok));
      tree[tl].index(tok, id, ((ScoringTokenizer) wp).score(tok));
    } else tree[tl].index(tok, id, wp.pos);
  }

  @Override
  void write() throws IOException {
    final String db = data.meta.name;
    final Prop pr = data.meta.prop;
    final DataOutput outx = new DataOutput(pr.dbfile(db, DATAFTX + 'x'));
    final DataOutput outy = new DataOutput(pr.dbfile(db, DATAFTX + 'y'));
    final DataOutput outz = new DataOutput(pr.dbfile(db, DATAFTX + 'z'));

    // write index size
    outx.write(isize);
    long dr = 0;
    int tr = 0;
    byte j = 1;
    for(int c = 0; j < tree.length && c < isize - 1; j++) {
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
        final int pres = Num.size(vpre);
        final int poss = Num.size(vpos);
        while(lpre < pres && lpos < poss) {
          for(int z = 0, l = Num.len(vpre, lpre); z < l; z++)
            outz.write(vpre[lpre++]);
          for(int z = 0, l = Num.len(vpos, lpos); z < l; z++)
            outz.write(vpos[lpos++]);
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
}

package org.basex.index;

import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.io.DataOutput;
import org.basex.util.IntArrayList;
import org.basex.util.TokenList;

/**
 * This class builds an index for text contents in a compressed trie.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Sebastian Gath
 * @author Christian Gruen
 */
public final class FTTrieBuilder extends FTBuilder {
  /** Trie index. */
  private final FTArray index = new FTArray(128);
  /** Hash structure for temporarily saving the tokens. */
  private FTHash hash = new FTHash();

  /**
   * Constructor.
   * @param d data reference
   * @param pr database properties
   */
  public FTTrieBuilder(final Data d, final Prop pr) {
    super(d, pr);
  }

  @Override
  public FTIndex build() throws IOException {
    index();
    return new FTTrie(data);
  }

  @Override
  void index(final byte[] tok) {
    hash.index(tok, pre, wp.pos);
  }

  @Override
  int nrTokens() {
    return hash.size();
  }

  @Override
  void getFreq() {
    hash.init();
    while(hash.more()) getFreq(hash.pre[hash.next()]);
  }

  @Override
  void write() throws IOException {
    final String db = data.meta.name;
    final Prop pr = data.meta.prop;
    final DataOutput outb = new DataOutput(pr.dbfile(db, DATAFTX + 'b'));

    if(scm == 0) hash.init();
    else hash.initIter();
    while(hash.more()) {
      final int p = hash.next();
      final byte[] tok = hash.key();
      final int ds = hash.ns[p];
      final long cpre = outb.size();
      // write compressed pre and pos arrays
      writeFTData(outb, hash.pre[p], hash.pos[p]);
      index.insertSorted(tok, ds, cpre);
    }

    hash = null;

    final TokenList tokens = index.tokens;
    final IntArrayList next = index.next;

    // save each node: l, t1, ..., tl, n1, v1, ..., nu, vu, s, p
    // l = length of the token t1, ..., tl
    // u = number of next nodes n1, ..., nu
    // v1= the first byte of each token n1 points, ...
    // s = size of pre values saved at pointer p
    // [byte, byte[l], byte, int, byte, ..., int, long]
    final DataOutput outN = new DataOutput(pr.dbfile(db, DATAFTX + 'a'));
    // ftdata is stored here, with pre1, ..., preu, pos1, ..., posu
    // each node entries size is stored here
    final DataOutput outS = new DataOutput(pr.dbfile(db, DATAFTX + 'c'));

    // document contains any text nodes -> empty index created;
    // only root node is kept
    int s = 0;
    if(index.count != 1) {
      // index.next[i] : [p, n1, ..., s, d]
      // index.tokens[p], index.next[n1], ..., index.pre[d]

      // first root node
      // write token size as byte
      outN.write((byte) 1);
      // write token
      outN.write((byte) -1);
      // write next pointer
      int j = 1, js = next.get(0).length - 2;
      for(; j < js; j++) {
        outN.writeInt(next.get(0)[j]); // pointer
        // first char of next node
        outN.write(tokens.get(next.get(next.get(0)[j])[0])[0]);
      }

      outN.writeInt(next.get(0)[j]); // data size
      outN.write5(-1); // pointer on data - root has no data
      outS.writeInt(s);
      s += 2L + (next.get(0).length - 3) * 5L + 9L;
      // all other nodes
      final int il = next.size();
      for(int i = 1; i < il; i++) {
        final int[] nxt = next.get(i);
        // check pointer on data needs 1 or 2 ints
        final int lp = nxt[nxt.length - 1] > -1 ? 0 : -1;
        // write token size as byte
        outN.write((byte) tokens.get(nxt[0]).length);
        // write token
        outN.write(tokens.get(nxt[0]));
        // write next pointer
        j = 1;
        for(; j < nxt.length - 2 + lp; j++) {
          outN.writeInt(nxt[j]); // pointer
          // first char of next node
          outN.write(tokens.get(next.get(nxt[j])[0])[0]);
        }
        outN.writeInt(nxt[j]); // data size
        if(nxt[j] == 0 && nxt[j + 1] == 0) {
          // node has no data
          outN.write5(nxt[j + 1]);
        } else {
          // write pointer on data
          if(lp == 0) {
            outN.write5(nxt[j + 1]);
          } else {
            outN.write5(toLong(nxt, nxt.length - 2));
          }
        }
        outS.writeInt(s);
        s += 1L + tokens.get(nxt[0]).length * 1L + (nxt.length - 3 + lp)
            * 5L + 9L;
      }
    }
    outS.writeInt(s);
    outb.close();
    outN.close();
    outS.close();
  }

  /**
   * Converts long values split with toArray back.
   * @param ar int[] with values
   * @param p pointer where the first value is found
   * @return long l
   */
  private static long toLong(final int[] ar, final int p) {
    long l = (long) ar[p] << 16;
    l += -ar[p + 1] & 0xFFFF;
    return l;
  }
}

package org.basex.index.value;

import static org.basex.core.Text.*;
import org.basex.data.Data;
import org.basex.index.IndexIterator;
import org.basex.index.IndexStats;
import org.basex.index.IndexToken;
import org.basex.util.Array;
import org.basex.util.TokenBuilder;

/**
 * This class provides a main memory access to attribute values and
 * text contents.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class UpdatableMemValues extends MemValues {
  /**
   * Constructor.
   * @param d data instance
   */
  public UpdatableMemValues(final Data d) {
    super(d);
  }

  @Override
  public void delete(final byte[] key, final int id) {
    final int i = id(key);
    if(i == 0 || len[i] == 0) return;

    // find the position where the id is stored
    int p = -1;
    while(++p < len[i]) if(ids[i][p] == id) break;

    // if not the last element, we need to shift forwards
    if(p < len[i] - 1) Array.move(ids[i], p + 1, -1, len[i] - (p + 1));
    len[i]--;
  }

  @Override
  public IndexIterator iter(final IndexToken tok) {
    final int i = id(tok.get());
    if(i > 0) {
      final int[] pres = data.pre(ids[i], 0, len[i]);
      if(pres.length > 0) {
        return new IndexIterator() {
          int p;
          @Override
          public boolean more() { return p < pres.length; }
          @Override
          public int next() { return pres[p++]; }
          @Override
          public double score() { return -1; }
          @Override
          public int size() { return pres.length; }
        };
      }
    }
    return IndexIterator.EMPTY;
  }

  @Override
  public byte[] info() {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(INDEXSTRUC + TREESTRUC + NL);
    final IndexStats stats = new IndexStats(data);
    for(int m = 1; m < size; ++m) {
      final int oc = len[m];
      if(stats.adding(oc)) stats.add(key(m));
    }
    stats.print(tb);
    return tb.finish();
  }
}

package org.basex.index.value;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.index.stats.*;
import org.basex.util.*;

/**
 * This class provides a main memory access to attribute values and
 * text contents.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class UpdatableMemValues extends MemValues {
  /**
   * Constructor.
   * @param d data instance
   */
  public UpdatableMemValues(final MemData d) {
    super(d);
  }

  @Override
  public IndexIterator iter(final IndexToken tok) {
    final byte k = tok.type() == IndexType.TEXT ? Data.TEXT : Data.ATTR;
    final int i = id(tok.get());
    if(i > 0) {
      final int[] pres = data.pre(ids[i], 0, len[i]);
      final int s = pres.length;
      if(s > 0) {
        return new IndexIterator() {
          int p;
          @Override
          public boolean more() { return p < s; }
          @Override
          public int pre() {
            while(more() && data.kind(pres[p++]) != k);
            return pres[p - 1];
          }
          @Override
          public int size() {
            return s;
          }
        };
      }
    }
    return IndexIterator.EMPTY;
  }

  @Override
  public byte[] info() {
    final TokenBuilder tb = new TokenBuilder(LI_STRUCTURE).add(SORTED_LIST).add(NL);
    final IndexStats stats = new IndexStats(data.meta.options.get(MainOptions.MAXSTAT));
    for(int m = 1; m < size; ++m) {
      if(stats.adding(len[m])) stats.add(key(m));
    }
    stats.print(tb);
    return tb.finish();
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
}

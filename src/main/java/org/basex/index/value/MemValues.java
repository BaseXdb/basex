package org.basex.index.value;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.util.Arrays;

import org.basex.data.Data;
import org.basex.index.Index;
import org.basex.index.IndexIterator;
import org.basex.index.IndexStats;
import org.basex.index.IndexToken;
import org.basex.util.Array;
import org.basex.util.TokenBuilder;
import org.basex.util.hash.TokenIntMap;
import org.basex.util.hash.TokenSet;

/**
 * This class provides a main memory access to attribute values and
 * text contents.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public class MemValues extends TokenSet implements Index {
  /** IDs. */
  protected int[][] ids = new int[CAP][];
  /** ID array lengths. */
  protected int[] len = new int[CAP];
  /** Data instance. */
  protected final Data data;

  /**
   * Constructor.
   * @param d data instance
   */
  public MemValues(final Data d) {
    data = d;
  }

  /**
   * Indexes the specified keys and values.
   * @param key key
   * @param id id value
   * @return index position
   */
  public int index(final byte[] key, final int id) {
    int i = add(key);
    if(i > 0) {
      ids[i] = new int[] { id };
    } else {
      i = -i;
      final int l = len[i];
      if(l == ids[i].length) ids[i] = Arrays.copyOf(ids[i], l << 1);
      ids[i][l] = id;
    }
    len[i]++;
    return i;
  }

  /**
   * Remove record from the index.
   * @param key record key
   * @param id record id
   */
  @SuppressWarnings("unused")
  public void delete(final byte[] key, final int id) { }

  @Override
  public IndexIterator iter(final IndexToken tok) {
    final int i = id(tok.get());
    if(i > 0) {
      final int cnt = len[i];
      if(cnt > 0) {
        final int[] pres = ids[i];
        return new IndexIterator() {
          private int p;
          @Override
          public boolean more() { return p < cnt; }
          @Override
          public int next() { return pres[p++]; }
          @Override
          public double score() { return -1; }
          @Override
          public int size() { return cnt; }
        };
      }
    }
    return IndexIterator.EMPTY;
  }

  @Override
  public int count(final IndexToken it) {
    final int i = id(it.get());
    return i == 0 ? 0 : len[i];
  }

  @Override
  public TokenIntMap entries(final byte[] prefix) {
    final TokenIntMap tim = new TokenIntMap();
    for(int m = 1; m < size; ++m) {
      if(startsWith(keys[m], prefix)) tim.add(keys[m], len[m]);
    }
    return tim;
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

  @Override
  public void close() { }

  @Override
  public void rehash() {
    super.rehash();
    final int s = size << 1;
    ids = Array.copyOf(ids, s);
    len = Arrays.copyOf(len, s);
  }
}

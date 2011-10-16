package org.basex.index.value;

import java.util.Arrays;

import org.basex.data.Data;
import org.basex.index.Index;
import org.basex.index.IndexIterator;
import org.basex.index.IndexToken;
import org.basex.util.Array;
import org.basex.util.Token;
import org.basex.util.Util;
import org.basex.util.hash.TokenSet;

/**
 * This class provides a main memory access to attribute values and
 * text contents.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class MemValues extends TokenSet implements Index {
  /** IDs. */
  private int[][] ids = new int[CAP][];
  /** ID array lengths. */
  private int[] len = new int[CAP];
  /** ID->PRE mapping. */
  private final Data data;

  /**
   * Constructor.
   * @param m id->pre mapping
   */
  public MemValues(final Data m) {
    data = m;
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
          int p = -1;
          @Override
          public boolean more() { return ++p < pres.length; }
          @Override
          public int next() { return pres[p]; }
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
  public int count(final IndexToken it) {
    final int i = id(it.get());
    return i == 0 ? 0 : len[i];
  }

  @Override
  public byte[] info() {
    return Token.token(Util.name(this));
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

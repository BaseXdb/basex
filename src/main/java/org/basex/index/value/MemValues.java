package org.basex.index.value;

import java.util.Arrays;

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
  int[][] ids = new int[CAP][];
  /** ID array lengths. */
  int[] len = new int[CAP];

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

  @Override
  public IndexIterator ids(final IndexToken tok) {
    final int i = id(tok.get());
    if(i == 0) return IndexIterator.EMPTY;

    return new IndexIterator() {
      int p = -1;
      @Override
      public boolean more() { return ++p < len[i]; }
      @Override
      public int next() { return ids[i][p]; }
      @Override
      public double score() { return -1; }
      @Override
      public int size() { return len[i]; }
    };
  }

  @Override
  public int nrIDs(final IndexToken it) {
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

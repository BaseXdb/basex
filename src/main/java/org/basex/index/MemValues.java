package org.basex.index;

import java.util.Arrays;
import org.basex.util.Array;
import org.basex.util.TokenSet;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * This class provides a main memory access to attribute values and
 * text contents.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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

  /**
   * Returns the key for the specified id.
   * @param id id
   * @return token
   */
  public byte[] get(final int id) {
    return key(id);
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
    };
  }

  @Override
  public int nrIDs(final IndexToken it) {
    return ids(it).size();
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

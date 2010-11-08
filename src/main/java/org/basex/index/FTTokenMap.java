package org.basex.index;

import java.util.Arrays;
import org.basex.util.TokenSet;

/**
 * This class caches sizes and pointers from full-text results.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Sebastian Gath
 */
final class FTTokenMap extends TokenSet {
  /** Number of position values. */
  private int[] sizes = new int[CAP];
  /** Pointer on token data. */
  private long[] pointers = new long[CAP];

  /**
   * Indexes the specified keys and values.
   * @param key key
   * @param s size
   * @param p pointer
   */
  void add(final byte[] key, final int s, final long p) {
    final int i = add(key);
    if(i > 0) {
      sizes[i] = s;
      pointers[i] = p;
    }
  }

  /**
   * Returns the value for the specified key.
   * @param id key to be found
   * @return sized
   */
  int size(final int id) {
    return sizes[id];
  }

  /**
   * Returns the pointer for the specified key.
   * @param id key to be found
   * @return pointer
   */
  long pointer(final int id) {
    return pointers[id];
  }

  @Override
  protected void rehash() {
    super.rehash();
    final int s = size << 1;
    pointers = Arrays.copyOf(pointers, s);
    sizes = Arrays.copyOf(sizes, s);
  }
}

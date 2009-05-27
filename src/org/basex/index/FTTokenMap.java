package org.basex.index;

import org.basex.util.Array;
import org.basex.util.Set;

/**
 * This class caches sizes and pointers from full-text results.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Sebastian Gath
 */
final class FTTokenMap extends Set {
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
  int getSize(final int id) {
    return sizes[id];
  }

  /**
   * Returns the pointer for the specified key.
   * @param id key to be found
   * @return pointer
   */
  long getPointer(final int id) {
    return pointers[id];
  }
  
  @Override
  protected void rehash() {
    super.rehash();
    pointers = Array.extend(pointers);
    sizes = Array.extend(sizes);
  }
}

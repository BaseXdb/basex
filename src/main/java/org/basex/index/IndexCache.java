package org.basex.index;

import java.util.Arrays;

import org.basex.util.hash.TokenSet;

/**
 * This class caches sizes and pointers from index results.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Sebastian Gath
 */
public final class IndexCache extends TokenSet {
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
  public void add(final byte[] key, final int s, final long p) {
    final int i = add(key);
    if(i > 0) {
      sizes[i] = s;
      pointers[i] = p;
    }
  }

  /**
   * Indexes the specified keys and values.
   * @param id cache id of the key
   * @param s size
   * @param p pointer
   */
  public void update(final int id, final int s, final long p) {
    sizes[id] = s;
    pointers[id] = p;
  }

  /**
   * Returns the size for the specified key.
   * @param id id of the key to be found
   * @return size
   */
  public int size(final int id) {
    return sizes[id];
  }

  /**
   * Returns the pointer for the specified key.
   * @param id id of the key to be found
   * @return pointer
   */
  public long pointer(final int id) {
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

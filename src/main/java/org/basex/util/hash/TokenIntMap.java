package org.basex.util.hash;

import java.util.Arrays;

/**
 * This is an efficient hash map for integers,
 * extending the {@link TokenSet hash set}.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class TokenIntMap extends TokenSet {
  /** Values. */
  private int[] values = new int[CAP];

  /**
   * Indexes the specified keys and values.
   * If the entry exists, the old value is replaced.
   * @param key key
   * @param val value
   */
  public void add(final byte[] key, final int val) {
    final int i = add(key);
    values[Math.abs(i)] = val;
  }

  /**
   * Returns the value for the specified key.
   * @param key key to be found
   * @return value or -1 if nothing was found
   */
  public int get(final byte[] key) {
    final int id = id(key);
    return id == 0 ? -1 : values[id];
  }

  @Override
  protected void rehash() {
    super.rehash();
    values = Arrays.copyOf(values, size << 1);
  }
}

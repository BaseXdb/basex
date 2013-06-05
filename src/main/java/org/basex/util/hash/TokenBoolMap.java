package org.basex.util.hash;

import java.util.*;

import org.basex.util.*;

/**
 * This is an efficient and memory-saving hash map for storing tokens and booleans.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class TokenBoolMap extends TokenSet {
  /** Values. */
  private boolean[] values;

  /**
   * Constructor.
   */
  public TokenBoolMap() {
    values = new boolean[Array.CAPACITY];
  }

  /**
   * Indexes the specified key and stores the associated value.
   * If the key already exists, the value is updated.
   * @param key key
   * @param value value
   */
  public void put(final byte[] key, final boolean value) {
    // array bounds are checked before array is resized..
    final int i = put(key);
    values[i] = value;
  }

  /**
   * Returns the value for the specified key.
   * @param key key to be looked up
   * @return value, or {@code -1} if the key was not found
   */
  public boolean get(final byte[] key) {
    return values[id(key)];
  }

  @Override
  protected void rehash() {
    super.rehash();
    values = Arrays.copyOf(values, size << 1);
  }
}

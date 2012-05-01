package org.basex.util.hash;

import java.util.*;

/**
 * This is an efficient hash map for booleans, extending the {@link TokenSet hash set}.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class TokenBoolMap extends TokenSet {
  /** Values. */
  private boolean[] values;

  /**
   * Constructor.
   */
  public TokenBoolMap() {
    values = new boolean[CAP];
  }

  /**
   * Indexes the specified keys and values.
   * If the entry exists, the old value is replaced.
   * @param key key
   * @param val value
   */
  public void add(final byte[] key, final boolean val) {
    final int i = add(key);
    values[Math.abs(i)] = val;
  }

  /**
   * Returns the specified value.
   * @param i index
   * @return value
   */
  public boolean value(final int i) {
    return values[i];
  }

  @Override
  protected void rehash() {
    super.rehash();
    values = Arrays.copyOf(values, size << 1);
  }
}

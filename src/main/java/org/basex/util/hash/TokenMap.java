package org.basex.util.hash;

import org.basex.util.*;

/**
 * This is an efficient hash map for tokens,
 * extending the {@link TokenSet hash set}.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class TokenMap extends TokenSet {
  /** Hash values. */
  private byte[][] values = new byte[CAP][];

  /**
   * Indexes the specified keys and values.
   * If the key exists, the value is updated.
   * @param key key
   * @param val value
   */
  public final void add(final byte[] key, final byte[] val) {
    // array bounds are checked before array is resized..
    final int i = add(key);
    values[Math.abs(i)] = val;
  }

  /**
   * Returns the value for the specified key.
   * @param key key to be found
   * @return value or {@code null} if nothing was found
   */
  public final byte[] get(final byte[] key) {
    return key != null ? values[id(key)] : null;
  }

  @Override
  public int delete(final byte[] key) {
    final int i = super.delete(key);
    values[i] = null;
    return i;
  }

  /**
   * Returns the specified value.
   * @param p value index
   * @return value
   */
  public final byte[] value(final int p) {
    return values[p];
  }

  @Override
  protected final void rehash() {
    super.rehash();
    values = Array.copyOf(values, size << 1);
  }

  @Override
  public final String toString() {
    final TokenBuilder tb = new TokenBuilder("TokenMap[");
    final byte[][] ks = keys();
    for(int i = 0; i < ks.length; i++) {
      tb.add(ks[i]).add(" = ").add(get(ks[i]));
      if(i < ks.length - 1) tb.add(", ");
    }
    return tb.add(']').toString();
  }
}

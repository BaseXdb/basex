package org.basex.util;

/**
 * This is a simple hash map, extending the even simpler
 * {@link TokenSet hash set}.
 *
 * @author BaseX Team 2005-11, BSD License
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
  public void add(final byte[] key, final byte[] val) {
    // array bounds are checked before array is resized..
    final int i = add(key);
    values[Math.abs(i)] = val;
  }

  /**
   * Returns the value for the specified key.
   * @param key key to be found
   * @return value or {@code null} if nothing was found
   */
  public byte[] get(final byte[] key) {
    return key != null ? values[id(key)] : null;
  }

  /**
   * Returns the specified value.
   * @param p value index
   * @return value
   */
  public byte[] value(final int p) {
    return values[p];
  }

  @Override
  protected void rehash() {
    super.rehash();
    values = Array.copyOf(values, size << 1);
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder("TokenMap[");
    final byte[][] ks = keys();
    for(int i = 0; i < ks.length; i++) {
      tb.add(ks[i]).add(" = ").add(get(ks[i]));
      if(i < ks.length - 1) tb.add(", ");
    }
    return tb.add(']').toString();
  }
}

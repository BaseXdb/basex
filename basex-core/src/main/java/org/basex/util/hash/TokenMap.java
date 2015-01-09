package org.basex.util.hash;

import org.basex.util.*;

/**
 * This is an efficient and memory-saving hash map for storing tokens.
 * It extends the {@link TokenSet} class.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public class TokenMap extends TokenSet {
  /** Hash values. */
  private byte[][] values = new byte[Array.CAPACITY][];

  /**
   * Stores the specified key and value.
   * If the key exists, the value is updated.
   * @param key key
   * @param value value
   */
  public final void put(final byte[] key, final byte[] value) {
    // array bounds are checked before array is resized..
    final int i = put(key);
    values[i] = value;
  }

  /**
   * Convenience function for adding strings, which will be converted to tokens.
   * @param key key
   * @param value value
   */
  public final void put(final String key, final String value) {
    put(Token.token(key), Token.token(value));
  }

  /**
   * Returns the value for the specified key.
   * @param key key to be looked up
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
   * Returns a value iterator.
   * @return iterator
   */
  public final Iterable<byte[]> values() {
    return new ArrayIterator<>(values, 1, size);
  }

  @Override
  protected final void rehash(final int sz) {
    super.rehash(sz);
    values = Array.copyOf(values, sz);
  }

  @Override
  public final String toString() {
    final TokenBuilder tb = new TokenBuilder();
    for(int i = 1; i < size; i++) {
      if(!tb.isEmpty()) tb.add(", ");
      if(keys[i] != null) tb.add(keys[i]).add(" = ").add(values[i]);
    }
    return new TokenBuilder(Util.className(getClass())).add('[').add(tb.finish()).
        add(']').toString();
  }
}

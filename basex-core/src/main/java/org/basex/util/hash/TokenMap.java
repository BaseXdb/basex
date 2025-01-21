package org.basex.util.hash;

import java.util.*;

import org.basex.util.*;

/**
 * This is an efficient and memory-saving hash map for storing tokens.
 * It extends the {@link TokenSet} class.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class TokenMap extends TokenSet {
  /** Map values. */
  private byte[][] values;

  /**
   * Default constructor.
   */
  public TokenMap() {
    this(Array.INITIAL_CAPACITY);
  }

  /**
   * Constructor with initial capacity.
   * @param capacity array capacity (will be resized to a power of two)
   */
  public TokenMap(final int capacity) {
    super(capacity);
    values = new byte[capacity()][];
  }

  /**
   * Stores the specified key and value.
   * If the key exists, the value is updated.
   * @param key key
   * @param value value
   */
  public void put(final byte[] key, final byte[] value) {
    // array bounds are checked before array is resized
    final int i = put(key);
    values[i] = value;
  }

  /**
   * Convenience function for adding strings, which will be converted to tokens.
   * @param key key
   * @param value value
   */
  public void put(final String key, final String value) {
    put(Token.token(key), Token.token(value));
  }

  /**
   * Returns the value for the specified key.
   * @param key key to be looked up
   * @return value, or {@code null} if nothing was found
   */
  public byte[] get(final byte[] key) {
    return key != null ? values[id(key)] : null;
  }

  @Override
  public int remove(final byte[] key) {
    final int i = super.remove(key);
    values[i] = null;
    return i;
  }

  /**
   * Returns a value iterator.
   * @return iterator
   */
  public Iterable<byte[]> values() {
    return new ArrayIterator<>(values, 1, size);
  }

  @Override
  public void clear() {
    Arrays.fill(values, null);
    super.clear();
  }

  @Override
  protected void rehash(final int newSize) {
    super.rehash(newSize);
    values = Array.copyOf(values, newSize);
  }

  @Override
  public String toString() {
    return toString(keys, values);
  }
}

package org.basex.util.hash;

import java.util.Arrays;

/**
 * This is an efficient hash map for generic objects,
 * extending the {@link TokenSet hash set}.
 * @param <E> generic value type
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class TokenObjMap<E> extends TokenSet {
  /** Values. */
  private Object[] values = new Object[CAP];

  /**
   * Indexes the specified keys and values.
   * If the key exists, the value is updated.
   * @param key key
   * @param val value
   */
  public void add(final byte[] key, final E val) {
    // array bounds are checked before array is resized..
    final int i = add(key);
    values[Math.abs(i)] = val;
  }

  /**
   * Returns the value for the specified key.
   * @param key key to be found
   * @return value or {@code null} if nothing was found
   */
  @SuppressWarnings("unchecked")
  public E get(final byte[] key) {
    return key != null ? (E) values[id(key)] : null;
  }

  /**
   * Returns the specified value.
   * @param p value index
   * @return value
   */
  @SuppressWarnings("unchecked")
  public E value(final int p) {
    return (E) values[p];
  }

  @Override
  public int delete(final byte[] key) {
    final int i = super.delete(key);
    values[i] = null;
    return i;
  }

  @Override
  protected void rehash() {
    super.rehash();
    values = Arrays.copyOf(values, size << 1);
  }
}

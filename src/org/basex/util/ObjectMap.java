package org.basex.util;

import java.util.Arrays;

/**
 * This is a simple hash map, extending the even simpler
 * {@link TokenSet hash set}.
 * @param <E> generic value type
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class ObjectMap<E> extends TokenSet {
  /** Values. */
  private Object[] values = new Object[CAP];

  /**
   * Indexes the specified keys and values.
   * If the entry exists, the old value is replaced.
   * @param key key
   * @param val value
   */
  public final void put(final byte[] key, final E val) {
    // array bounds are checked before array is resized..
    final int i = add(key);
    values[Math.abs(i)] = val;
  }

  /**
   * Returns the value for the specified key.
   * @param key key to be found
   * @return value or null if nothing was found
   */
  @SuppressWarnings("unchecked")
  public final E get(final byte[] key) {
    return key != null ? (E) values[id(key)] : null;
  }

  /**
   * Returns the specified value.
   * @param p value index
   * @return value
   */
  @SuppressWarnings("unchecked")
  public final E value(final int p) {
    return (E) values[p];
  }

  @Override
  protected final void rehash() {
    super.rehash();
    values = Arrays.copyOf(values, size << 1);
  }
}

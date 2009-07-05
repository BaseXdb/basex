package org.basex.util;

/**
 * This is a simple hash map, extending the even simpler
 * {@link Set hash set}.
 * @param <E> generic value type
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class Map<E> extends Set {
  /** Values. */
  private Object[] values = new Object[CAP];

  /**
   * Indexes the specified keys and values.
   * If the entry exists, the old value is replaced.
   * @param key key
   * @param val value
   */
  public void add(final byte[] key, final E val) {
    final int i = add(key);
    values[Math.abs(i)] = val;
  }

  /**
   * Returns the value for the specified key.
   * @param key key to be found
   * @return value or null if nothing was found
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
  protected void rehash() {
    super.rehash();
    values = Array.extend(values);
  }
}

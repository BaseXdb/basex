package org.basex.util.hash;

import org.basex.util.*;

/**
 * This is an efficient and memory-saving hash map for storing tokens and objects.
 * {@link TokenSet hash set}.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 * @param <E> generic value type
 */
public final class TokenObjMap<E> extends TokenSet {
  /** Values. */
  private Object[] values = new Object[Array.CAPACITY];

  /**
   * Indexes the specified key and value.
   * If the key exists, the value is updated.
   * @param key key
   * @param val value
   */
  public void put(final byte[] key, final E val) {
    // array bounds are checked before array is resized..
    final int i = put(key);
    values[i] = val;
  }

  /**
   * Returns the value for the specified key.
   * @param key key to be looked up
   * @return value or {@code null} if nothing was found
   */
  @SuppressWarnings("unchecked")
  public E get(final byte[] key) {
    return key != null ? (E) values[id(key)] : null;
  }

  /**
   * Returns a value iterator.
   * @return iterator
   */
  public Iterable<E> values() {
    return new ArrayIterator<>(values, 1, size);
  }

  @Override
  public int delete(final byte[] key) {
    final int i = super.delete(key);
    values[i] = null;
    return i;
  }

  @Override
  protected void rehash(final int sz) {
    super.rehash(sz);
    values = Array.copy(values, new Object[sz]);
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder();
    for(final byte[] key : this) {
      if(!tb.isEmpty()) tb.add(", ");
      if(key != null) {
        final Object val = values[id(key)];
        tb.add('{').add(key).add(',').add(val == null ? "null" :  val.toString()).add('}');
      }
    }
    return new TokenBuilder(Util.className(getClass())).add('[').add(tb.finish()).
      add(']').toString();
  }
}

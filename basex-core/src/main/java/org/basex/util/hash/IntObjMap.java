package org.basex.util.hash;

import org.basex.util.*;

/**
 * This is an efficient and memory-saving hash map for storing primitive integers
 * and objects. It extends the {@link IntSet} class.
 * @param <E> generic value type
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class IntObjMap<E> extends IntSet {
  /** Values. */
  private Object[] values = new Object[Array.CAPACITY];

  /**
   * Indexes the specified key and stores the associated value.
   * If the key already exists, the value is updated.
   * @param key key
   * @param value value
   */
  public void put(final int key, final E value) {
    // array bounds are checked before array is resized..
    final int i = put(key);
    values[i] = value;
  }

  /**
   * Returns the value for the specified key.
   * @param key key to be looked up
   * @return value, or {@code null} if the key was not found
   */
  @SuppressWarnings("unchecked")
  public E get(final int key) {
    return (E) values[id(key)];
  }

  /**
   * Returns a value iterator.
   * @return iterator
   */
  public Iterable<E> values() {
    return new ArrayIterator<E>(values, 1, size);
  }

  @Override
  protected void rehash(final int s) {
    super.rehash(s);
    values = Array.copy(values, new Object[s]);
  }

  @Override
  public int delete(final int key) {
    final int i = super.delete(key);
    if(i != 0) values[i] = null;
    return i;
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(Util.className(this)).add('[');
    for(int i = 1; i < size; i++) {
      tb.add(Integer.toString(keys[i])).add(": ").add(get(keys[i]).toString());
      if(i < size - 1) tb.add(",\n\t");
    }
    return tb.add(']').toString();
  }
}

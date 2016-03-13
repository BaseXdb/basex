package org.basex.util.hash;

import java.util.*;

import org.basex.util.*;

/**
 * This is an efficient and memory-saving hash map for storing primitive integers
 * and objects. It extends the {@link IntSet} class.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 * @param <E> generic value type
 */
public final class IntObjMap<E> extends IntSet {
  /** Values. */
  private Object[] values = new Object[Array.CAPACITY];

  /**
   * Indexes the specified key and stores the associated value.
   * If the key already exists, the value is updated.
   * @param key key
   * @param value value
   * @return old value
   */
  @SuppressWarnings("unchecked")
  public E put(final int key, final E value) {
    // array bounds are checked before array is resized..
    final int i = put(key);
    final Object v = values[i];
    values[i] = value;
    return (E) v;
  }

  /**
   * Returns the value for the specified key.
   * @param key key to be looked up
   * @return value or {@code null} if the key was not found
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
    return new ArrayIterator<>(values, 1, size);
  }

  @Override
  protected void rehash(final int sz) {
    super.rehash(sz);
    values = Array.copy(values, new Object[sz]);
  }

  @Override
  public int delete(final int key) {
    final int i = super.delete(key);
    if(i != 0) values[i] = null;
    return i;
  }

  @Override
  public void clear() {
    super.clear();
    Arrays.fill(values, null);
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

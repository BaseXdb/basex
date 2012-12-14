package org.basex.util.hash;

import java.util.*;

import org.basex.util.*;

/**
 * This is an efficient hash map, extending the {@link IntSet hash set}.
 * @param <E> generic value type
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class IntMap<E> extends IntSet {
  /** Values. */
  private Object[] values = new Object[CAP];

  /**
   * Indexes the specified keys and values.
   * If the key exists, the value is updated.
   * @param key key
   * @param val value
   */
  public void add(final int key, final E val) {
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
  public E get(final int key) {
    return (E) values[id(key)];
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
    values = Arrays.copyOf(values, size << 1);
  }

  @Override
  public int delete(final int key) {
    int i = super.delete(key);
    if(i != 0) values[i] = null;
    return i;
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(Util.name(this)).add('[');
    for(int i = 1; i < size; i++) {
      tb.add(Integer.toString(keys[i])).add(": ").add(get(keys[i]).toString());
      if(i < size - 1) tb.add(",\n\t");
    }
    return tb.add(']').toString();
  }
}

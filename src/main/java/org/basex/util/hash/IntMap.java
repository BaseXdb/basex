package org.basex.util.hash;

import java.util.Arrays;

import org.basex.util.TokenBuilder;
import org.basex.util.Util;

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

  @Override
  protected void rehash() {
    super.rehash();
    values = Arrays.copyOf(values, size << 1);
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

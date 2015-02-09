package org.basex.query.util.hash;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Map for quickly indexing items.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class ItemValueMap extends HashItemSet {
  /** Values. */
  private Value[] values = new Value[Array.CAPACITY];

  /**
   * Indexes the specified key and stores the associated value.
   * If the key already exists, the value is updated.
   * @param key key
   * @param value value
   * @param ii input info
   * @throws QueryException query exception
   */
  public void add(final Item key, final Value value, final InputInfo ii) throws QueryException {
    // array bounds are checked before array is resized..
    final int i = put(key, ii);
    values[Math.abs(i)] = value;
  }

  /**
   * Returns the value for the specified key.
   * @param key key to be found
   * @param ii input info
   * @return value or {@code null} if nothing was found
   * @throws QueryException query exception
   */
  public Value get(final Item key, final InputInfo ii) throws QueryException {
    return values[id(key, ii)];
  }

  /**
   * Returns a value iterator.
   * @return iterator
   */
  public Iterable<Value> values() {
    return new ArrayIterator<>(values, 1, size);
  }

  @Override
  protected void rehash(final int s) {
    super.rehash(s);
    values = Array.copy(values, new Value[s]);
  }
}

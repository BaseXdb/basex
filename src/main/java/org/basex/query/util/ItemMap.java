package org.basex.query.util;

import java.util.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Map for quickly indexing items.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class ItemMap extends ItemSet {
  /** Values. */
  private Value[] values = new Value[CAP];

  /**
   * Indexes the specified keys and values.
   * If the key exists, the value is updated.
   * @param key key
   * @param val value
   * @param ii input info
   * @throws QueryException query exception
   */
  public void add(final Item key, final Value val, final InputInfo ii)
      throws QueryException {
    // array bounds are checked before array is resized..
    final int i = add(key, ii);
    values[Math.abs(i)] = val;
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

  @Override
  protected void rehash() {
    super.rehash();
    values = Arrays.copyOf(values, size << 1);
  }
}

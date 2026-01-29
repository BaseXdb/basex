package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Merger for duplicate map values.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class MapDuplicates {
  /**
   * Returns a merged version of the old and new value.
   * @param key key
   * @param old old (can be {@code null})
   * @param value value
   * @return merged value, or new value if the old one is {@code null}
   * @throws QueryException query exception
   */
  Value merge(final Item key, final Value old, final Value value) throws QueryException {
    return old != null ? get(key, old, value) : value;
  }

  /**
   * Merges the old and new value.
   * @param key key
   * @param old old
   * @param value value
   * @return merged value, or {@code null} if insertion of map entry can be skipped
   * @throws QueryException query exception
   */
  abstract Value get(Item key, Value old, Value value) throws QueryException;

  /**
   * Returns the result type.
   * @param st input type
   * @return result type
   */
  SeqType type(final SeqType st) {
    return st;
  }
}

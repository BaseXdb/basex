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
   * @param qc query context
   * @return merged value, or new value if the old one is {@code null}
   * @throws QueryException query exception
   */
  Value merge(final Item key, final Value old, final Value value, final QueryContext qc)
      throws QueryException {
    return old != null ? get(key, old, value, qc) : value;
  }

  /**
   * Merges the old and new value.
   * @param key key
   * @param old old
   * @param value value
   * @param qc query context
   * @return merged value, or {@code null} if insertion of map entry can be skipped
   * @throws QueryException query exception
   */
  abstract Value get(Item key, Value old, Value value, QueryContext qc) throws QueryException;

  /**
   * Returns the result type.
   * @param st input type
   * @return result type
   */
  SeqType type(final SeqType st) {
    return st;
  }

  /**
   * Returns a merger that is refined for the supplied value type.
   * @param st value type
   * @param cc compilation context
   * @return merger
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  MapDuplicates refine(final SeqType st, final CompileContext cc) throws QueryException {
    return this;
  }
}

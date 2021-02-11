package org.basex.query.func.unit;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Unit function.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
abstract class UnitFn extends StandardFunc {
  /**
   * Returns an error with the specified item as value.
   * @param item item (may be {@code null})
   * @return error
   * @throws QueryException query exception
   */
  final QueryException error(final Item item) throws QueryException {
    return (item == null ? UNIT_FAIL.get(info) :
      UNIT_FAIL_X.get(info, item.string(info))).value(item);
  }
}

package org.basex.query.func.unit;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Unit function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class UnitFn extends StandardFunc {
  /**
   * Returns an error with the specified item as value.
   * @param it item (may be {@code null})
   * @return error
   * @throws QueryException query exception
   */
  final QueryException error(final Item it) throws QueryException {
    return (it == null ? UNIT_ASSERT.get(info) :
      UNIT_MESSAGE_X.get(info, it.string(info))).value(it);
  }
}

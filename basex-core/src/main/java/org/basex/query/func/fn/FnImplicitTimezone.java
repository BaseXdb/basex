package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnImplicitTimezone extends DynamicFn {
  @Override
  protected DTDur item(final QueryContext qc) throws QueryException {
    return new DTDur(0, qc.dateTime().zone);
  }
}

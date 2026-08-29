package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnCurrentTime extends DynamicFn {
  @Override
  public Tim value(final QueryContext qc) throws QueryException {
    return qc.dateTime().time;
  }
}

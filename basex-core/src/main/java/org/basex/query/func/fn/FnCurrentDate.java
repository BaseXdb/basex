package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnCurrentDate extends DynamicFn {
  @Override
  public Dat value(final QueryContext qc) throws QueryException {
    return qc.dateTime().date;
  }
}

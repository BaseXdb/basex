package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnCurrentDate extends DynamicFn {
  @Override
  public Dat item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return qc.dateTime().date;
  }
}

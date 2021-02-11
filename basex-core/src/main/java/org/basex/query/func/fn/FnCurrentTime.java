package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnCurrentTime extends StandardFunc {
  @Override
  public Tim item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return qc.dateTime().time;
  }
}

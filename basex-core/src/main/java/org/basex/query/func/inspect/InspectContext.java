package org.basex.query.func.inspect;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class InspectContext extends StandardFunc {
  @Override
  public FNode item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return new PlainDoc(qc, info).context();
  }
}

package org.basex.query.func.session;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class SessionDelete extends SessionFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    session(qc).delete(toToken(exprs[0], qc));
    return Empty.VALUE;
  }
}

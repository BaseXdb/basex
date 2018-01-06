package org.basex.query.func.xquery;

import org.basex.query.*;
import org.basex.query.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class XQueryInvokeUpdate extends XQueryInvoke {
  @Override
  protected ItemList eval(final QueryContext qc) throws QueryException {
    return invoke(qc, true);
  }
}

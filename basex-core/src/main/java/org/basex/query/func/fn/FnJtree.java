package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnJtree extends StandardFunc {
  @Override
  public JNode value(final QueryContext qc) throws QueryException {
    return new JNode(arg(0).value(qc));
  }
}

package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnTrue extends StandardFunc {
  // will always be pre-evaluated
  @Override
  protected Bln item(final QueryContext qc) {
    return Bln.TRUE;
  }
}

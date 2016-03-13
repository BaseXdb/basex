package org.basex.query.func.prof;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class ProfVariables extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) {
    FnTrace.trace(token(qc.stack.dump()), null, qc);
    return null;
  }
}

package org.basex.query.func.proc;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ProcFork extends ProcFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    exec(qc, true);
    return Empty.VALUE;
  }
}

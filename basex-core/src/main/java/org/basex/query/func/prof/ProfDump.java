package org.basex.query.func.prof;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class ProfDump extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter ir = exprs[0].iter(qc);
    final byte[] label = exprs.length > 1 ? toToken(exprs[1], qc) : null;
    boolean empty = true;
    for(Item it; (it = ir.next()) != null;) {
      FnTrace.dump(it, label, info, qc);
      empty = false;
    }
    if(empty) FnTrace.dump(null, label, info, qc);
    return null;
  }
}

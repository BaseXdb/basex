package org.basex.query.func.store;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StoreRead extends StoreFn {
  @Override
  public Empty item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String name = toName(arg(0), qc);
    stores(qc).read(name, info, qc);
    return Empty.VALUE;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.lock((String) null) && super.accept(visitor);
  }
}

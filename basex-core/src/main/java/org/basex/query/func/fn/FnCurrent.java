package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.core.locks.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnCurrent extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value value = qc.current != null ? qc.current : qc.globalValue();
    if(value == null) throw NOCTX_X.get(info, this);
    return value;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.lock(Locking.CONTEXT, false) && super.accept(visitor);
  }
}

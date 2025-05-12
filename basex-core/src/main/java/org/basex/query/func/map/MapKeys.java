package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class MapKeys extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return toMap(arg(0), qc).keys();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr map = arg(0);
    final Type type = map.seqType().type;
    if(type instanceof final MapType mt) {
      exprType.assign(mt.keyType().seqType(Occ.ZERO_OR_MORE), map.structSize());
    }
    return this;
  }
}

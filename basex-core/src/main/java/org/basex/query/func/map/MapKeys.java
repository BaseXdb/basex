package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Leo Woerteler
 */
public final class MapKeys extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQMap map = toMap(exprs[0], qc);

    return map.keys();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Type type = exprs[0].seqType().type;
    if(type instanceof MapType) exprType.assign(((MapType) type).keyType());
    return this;
  }
}

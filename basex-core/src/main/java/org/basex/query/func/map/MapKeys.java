package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class MapKeys extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final XQMap map = toMap(arg(0), qc);
    return map.keys();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr map = arg(0);
    final Type type = map.seqType().type;
    if(type instanceof MapType) {
      final Type kt = ((MapType) type).keyType();
      exprType.assign(kt.seqType(Occ.ZERO_OR_MORE), map.structSize());
    }
    return this;
  }
}

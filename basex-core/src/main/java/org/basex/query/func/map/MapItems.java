package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class MapItems extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return toMap(arg(0), qc).itemsIter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return toMap(arg(0), qc).items(qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr map = arg(0);
    final Type type = map.seqType().type;
    if(type instanceof MapType) {
      final SeqType vt = ((MapType) type).valueType();
      exprType.assign(vt.with(Occ.ZERO_OR_MORE), vt.one() ? map.structSize() : -1);
    }
    return this;
  }
}

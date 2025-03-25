package org.basex.query.func.util;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class UtilMapPutAt extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQMap map = toMap(arg(0), qc);
    final long index = toLong(arg(1), qc);
    final Value value = arg(2).value(qc);

    final long size = map.structSize();
    return index > 0 && index <= size ? map.putAt((int) index - 1, value) : map;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr map = arg(0);
    final Type type = map.seqType().type;
    if(type instanceof MapType) {
      exprType.assign(((MapType) type).valueType().union(Occ.ZERO));
    }
    return this;
  }
}

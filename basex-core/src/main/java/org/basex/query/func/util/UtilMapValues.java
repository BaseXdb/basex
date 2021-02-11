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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class UtilMapValues extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQMap map = toMap(exprs[0], qc);

    final ValueBuilder vb = new ValueBuilder(qc);
    map.values(vb);
    return vb.value(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final FuncType ft = exprs[0].funcType();
    if(ft instanceof MapType) exprType.assign(ft.declType.with(Occ.ZERO_OR_MORE));
    return this;
  }
}

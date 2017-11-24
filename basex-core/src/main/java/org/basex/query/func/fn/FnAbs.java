package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnAbs extends StandardFunc {
  @Override
  public ANum item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANum num = toNumber(exprs[0], qc);
    return num == null ? null : num.abs();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    Type t = exprs[0].seqType().type;
    if(t.isUntyped()) t = AtomType.DBL;
    if(t.isNumber()) exprType.assign(t.instanceOf(AtomType.ITR) ? AtomType.ITR : t);
    return optFirst();
  }
}

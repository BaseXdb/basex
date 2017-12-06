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
    Type type = exprs[0].seqType().type;
    if(type.isUntyped()) type = AtomType.DBL;
    if(type.isNumber()) exprType.assign(type.instanceOf(AtomType.ITR) ? AtomType.ITR : type);
    return optFirst();
  }
}

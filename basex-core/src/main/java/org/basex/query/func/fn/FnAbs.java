package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnAbs extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANum num = toNumberOrNull(exprs[0], qc);
    return num == null ? Empty.VALUE : num.abs();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr expr = optFirst();
    if(expr != this) return expr;

    Type type = exprs[0].seqType().type;
    if(type.isUntyped()) type = AtomType.DOUBLE;
    if(type.isNumber()) {
      exprType.assign(type.instanceOf(AtomType.INTEGER) ? AtomType.INTEGER : type);
    }
    return this;
  }
}

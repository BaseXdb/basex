package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnAbs extends NumericFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANum value = toNumberOrNull(arg(0), qc);
    return value == null ? Empty.VALUE : value.abs();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr expr = optFirst();
    if(expr != this) return expr;
    final SeqType st = optType(arg(0), false);
    if(st != null) exprType.assign(st);
    return this;
  }
}

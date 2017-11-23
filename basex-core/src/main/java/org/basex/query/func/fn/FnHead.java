package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnHead extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return qc.iter(exprs[0]).next();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr ex = exprs[0];
    final SeqType st = ex.seqType();
    if(st.zeroOrOne()) return ex;
    if(ex instanceof Value) return ((Value) ex).itemAt(0);

    exprType.assign(st.type, st.oneOrMore() ? Occ.ONE : Occ.ZERO_ONE);

    return ex instanceof FnReverse ?
      cc.function(Function._UTIL_LAST_FROM, info, ((Arr) ex).exprs) : this;
  }
}

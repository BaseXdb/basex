package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnAvg extends FnSum {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    final Item item = sum(true, qc);
    return item != null ? item : Empty.VALUE;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    Expr expr = optFirst();
    if(expr != this) return expr;

    expr = opt(true, cc);
    if(expr != null) return expr;

    final SeqType st = arg(0).seqType();
    if(!st.mayBeWrapped()) exprType.assign(Calc.DIVIDE.type(st.type, BasicType.INTEGER));

    return this;
  }
}

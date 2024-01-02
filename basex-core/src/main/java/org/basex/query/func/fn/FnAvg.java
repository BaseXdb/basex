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
public class FnAvg extends FnSum {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item = sum(true, qc);
    return item != null ? item : Empty.VALUE;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    Expr expr = optFirst();
    if(expr != this) return expr;

    expr = opt(true);
    if(expr != null) return expr;

    final SeqType st = arg(0).seqType();
    if(!st.mayBeArray()) exprType.assign(Calc.DIVIDE.type(st.type, st.type));

    return this;
  }
}

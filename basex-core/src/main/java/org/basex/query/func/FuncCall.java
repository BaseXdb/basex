package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * An XQuery function call, either static or dynamic.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public abstract class FuncCall extends Arr {
  /** Tail-call flag. */
  boolean tco;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param exprs sub-expressions
   */
  FuncCall(final InputInfo info, final Expr[] exprs) {
    super(info, SeqType.ITEM_ZM, exprs);
  }

  /**
   * Evaluates a function item.
   * @param func function to be evaluated
   * @param qc query context
   * @return the function
   * @throws QueryException query exception
   */
  final Value evalFunc(final XQFunction func, final QueryContext qc) throws QueryException {
    final int arity = func.arity();
    final Value[] args = new Value[arity];
    for(int a = 0; a < arity; ++a) args[a] = exprs[a].value(qc);
    return tco ? func.invokeTail(qc, info, args) : func.invoke(qc, info, args);
  }

  @Override
  public final void markTailCalls(final CompileContext cc) {
    if(!tco) {
      if(cc != null) cc.info(QueryText.OPTTCE_X, this);
      tco = true;
    }
  }
}

package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * An XQuery function call, either static or dynamic.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Leo Woerteler
 */
public abstract class FuncCall extends Arr {
  /** Static context of this function call. */
  final StaticContext sc;
  /** Tail-call flag. */
  boolean tco;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param sc static context
   * @param exprs sub-expressions
   */
  FuncCall(final InputInfo info, final StaticContext sc, final Expr[] exprs) {
    super(info, SeqType.ITEM_ZM, exprs);
    this.sc = sc;
  }

  /**
   * Evaluates and returns the function to be called.
   * @param qc query context
   * @return the function
   * @throws QueryException query exception
   */
  abstract XQFunction evalFunc(QueryContext qc) throws QueryException;

  @Override
  public final void markTailCalls(final CompileContext cc) {
    if(!tco) {
      if(cc != null) cc.info(QueryText.OPTTCE_X, this);
      tco = true;
    }
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    final XQFunction func = evalFunc(qc);
    final int arity = func.arity();
    final Value[] args = new Value[arity];
    for(int a = 0; a < arity; ++a) args[a] = exprs[a].value(qc);
    return tco ? func.invokeTail(qc, info, args) : func.invoke(qc, info, args);
  }
}

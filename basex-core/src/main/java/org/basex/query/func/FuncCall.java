package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * An XQuery function call, either static or dynamic.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public abstract class FuncCall extends Arr {
  /** Tail-call flag. */
  boolean tco;

  /**
   * Constructor.
   * @param info input info
   * @param exprs sub-expressions
   */
  FuncCall(final InputInfo info, final Expr[] exprs) {
    super(info, SeqType.ITEM_ZM, exprs);
  }

  /**
   * Evaluates and returns the function to be called.
   * @param qc query context
   * @return the function
   * @throws QueryException query exception
   */
  abstract XQFunction evalFunc(QueryContext qc) throws QueryException;

  /**
   * Evaluates and returns the arguments for this call.
   * @param qc query context
   * @return the function
   * @throws QueryException query exception
   */
  abstract Value[] evalArgs(QueryContext qc) throws QueryException;

  @Override
  public final void markTailCalls(final CompileContext cc) {
    if(cc != null) cc.info(QueryText.OPTTCE_X, this);
    tco = true;
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    final XQFunction func = evalFunc(qc);
    final Value[] args = evalArgs(qc);
    return tco ? func.invokeTail(qc, info, args) : func.invoke(qc, info, args);
  }
}

package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * An XQuery function call, either static or dynamic.
 *
 * @author BaseX Team 2005-20, BSD License
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
    return tco ? invokeTail(func, args, false, qc) : func.invoke(args, false, qc, info);
  }

  @Override
  public final Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQFunction func = evalFunc(qc);
    final Value[] args = evalArgs(qc);
    return (Item) (tco ? invokeTail(func, args, true, qc) : func.invoke(args, true, qc, info));
  }

  /**
   * Tail-calls the given function with the given arguments.
   * @param func function to call
   * @param args arguments for the call
   * @param qc query context
   * @param item flag for requesting a single item
   * @return result of the function call
   * @throws QueryException query exception
   */
  private Value invokeTail(final XQFunction func, final Value[] args, final boolean item,
      final QueryContext qc) throws QueryException {

    qc.checkStop();
    final int calls = qc.tailCalls, max = qc.maxCalls;
    if(max >= 0 && calls >= max) {
      // too many tail calls on the stack, eliminate them
      qc.registerTailCall(func, args);
      return item ? null : Empty.VALUE;
    }

    qc.tailCalls++;
    final int fp = qc.stack.enterFrame(func.stackFrameSize());
    try {
      return func.invoke(qc, info, item, args);
    } catch(final QueryException ex) {
      throw ex.add(info);
    } finally {
      qc.tailCalls = calls;
      qc.stack.exitFrame(fp);
    }
  }
}

package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * An XQuery function call, either static or dynamic.
 *
 * @author BaseX Team 2005-19, BSD License
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
  public final Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    final XQFunction func = evalFunc(qc);
    final Value[] args = evalArgs(qc);
    return tco ? invokeTail(func, args, false, qc) : invoke(func, args, false, qc, info);
  }

  @Override
  public final Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQFunction func = evalFunc(qc);
    final Value[] args = evalArgs(qc);
    return (Item) (tco ? invokeTail(func, args, true, qc) : invoke(func, args, true, qc, info));
  }

  /**
   * Tail-calls the given function with the given arguments.
   * @param func function to call
   * @param arg arguments for the call
   * @param qc query context
   * @param item flag for requesting a single item
   * @return result of the function call
   * @throws QueryException query exception
   */
  private Value invokeTail(final XQFunction func, final Value[] arg, final boolean item,
      final QueryContext qc) throws QueryException {

    qc.checkStop();
    final int calls = qc.tailCalls, max = qc.maxCalls;
    if(max >= 0 && calls >= max) {
      // too many tail calls on the stack, eliminate them
      qc.registerTailCall(func, arg);
      return item ? null : Empty.SEQ;
    }

    qc.tailCalls++;
    final int fp = qc.stack.enterFrame(func.stackFrameSize());
    try {
      return item ? func.invItem(qc, info, arg) : func.invValue(qc, info, arg);
    } catch(final QueryException ex) {
      throw ex.add(info);
    } finally {
      qc.tailCalls = calls;
      qc.stack.exitFrame(fp);
    }
  }

  /**
   * Calls the given function with the given arguments and takes care of tail calls.
   * @param func function to call
   * @param args arguments for the call
   * @param qc query context
   * @param item flag for requesting a single item
   * @param ii input info
   * @return result of the function call
   * @throws QueryException query exception
   */
  public static Value invoke(final XQFunction func, final Value[] args, final boolean item,
      final QueryContext qc, final InputInfo ii) throws QueryException {

    XQFunction fn = func;
    Value[] vl = args;
    final int fp = qc.stack.enterFrame(fn.stackFrameSize());
    try {
      while(true) {
        qc.checkStop();
        final Value v = item ? fn.invItem(qc, ii, vl) : fn.invValue(qc, ii, vl);
        fn = qc.pollTailCall();
        if(fn == null) return v;
        qc.stack.reuseFrame(fn.stackFrameSize());
        vl = qc.pollTailArgs();
      }
    } catch(final QueryException ex) {
      throw ex.add(ii);
   } finally {
      qc.stack.exitFrame(fp);
    }
  }
}

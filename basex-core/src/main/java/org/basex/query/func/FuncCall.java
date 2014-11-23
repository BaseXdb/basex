package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * An XQuery function call, either static or dynamic.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public abstract class FuncCall extends Arr {
  /** Tail-call flag. */
  boolean tailCall;

  /**
   * Constructor.
   * @param info input info
   * @param exprs sub-expressions
   */
  FuncCall(final InputInfo info, final Expr[] exprs) {
    super(info, exprs);
  }

  /**
   * Evaluates and returns the function to be called.
   * @param qc query context
   * @return the function
   * @throws QueryException query exception
   */
  abstract XQFunction evalFunc(final QueryContext qc) throws QueryException;

  /**
   * Evaluates and returns the arguments for this call.
   * @param qc query context
   * @return the function
   * @throws QueryException query exception
   */
  abstract Value[] evalArgs(final QueryContext qc) throws QueryException;

  @Override
  public final void markTailCalls(final QueryContext qc) {
    if(qc != null) qc.compInfo(QueryText.OPTTCE, this);
    tailCall = true;
  }

  @Override
  public final Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return (Item) (tailCall ? invokeTail(evalFunc(qc), evalArgs(qc), true, qc, info)
                            : invoke(evalFunc(qc), evalArgs(qc), true, qc, info));
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    return tailCall ? invokeTail(evalFunc(qc), evalArgs(qc), false, qc, info)
                    : invoke(evalFunc(qc), evalArgs(qc), false, qc, info);
  }

  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  /**
   * Calls the given function with the given arguments and takes care of tail-calls.
   * @param fun function to call
   * @param arg arguments for the call
   * @param qc query context
   * @param itm flag for requesting a single item
   * @param ii input info
   * @return result of the function call
   * @throws QueryException query exception
   */
  private static Value invoke(final XQFunction fun, final Value[] arg, final boolean itm,
      final QueryContext qc, final InputInfo ii) throws QueryException {

    XQFunction func = fun;
    Value[] args = arg;
    final int fp = qc.stack.enterFrame(func.stackFrameSize());
    try {
      while(true) {
        final Value ret = itm ? func.invItem(qc, ii, args) : func.invValue(qc, ii, args);
        func = qc.pollTailCall();
        if(func == null) return ret;
        qc.stack.reuseFrame(func.stackFrameSize());
        args = qc.pollTailArgs();
      }
    } catch(final QueryException ex) {
      throw ex.add(ii);
   } finally {
      qc.stack.exitFrame(fp);
    }
  }

  /**
   * Tail-calls the given function with the given arguments.
   * @param fun function to call
   * @param arg arguments for the call
   * @param qc query context
   * @param itm flag for requesting a single item
   * @param ii input info
   * @return result of the function call
   * @throws QueryException query exception
   */
  private static Value invokeTail(final XQFunction fun, final Value[] arg, final boolean itm,
      final QueryContext qc, final InputInfo ii) throws QueryException {

    final int calls = qc.tailCalls, max = qc.maxCalls;
    if(max >= 0 && calls >= max) {
      // there are at least `ctx.maxCalls` tail-calls on the stack, eliminate them
      qc.registerTailCall(fun, arg);
      return itm ? null : Empty.SEQ;
    }

    qc.tailCalls++;
    final int fp = qc.stack.enterFrame(fun.stackFrameSize());
    try {
      return itm ? fun.invItem(qc, ii, arg) : fun.invValue(qc, ii, arg);
    } catch(final QueryException ex) {
      throw ex.add(ii);
    } finally {
      qc.tailCalls = calls;
      qc.stack.exitFrame(fp);
    }
  }

  /**
   * Calls the given function with the given arguments, returning zero or one item.
   * This method takes care of tail calls.
   * @param fun function to call
   * @param arg arguments to the function
   * @param qc query context
   * @param ii input info
   * @return the resulting item
   * @throws QueryException query exception
   */
  public static Item item(final XQFunction fun, final Value[] arg, final QueryContext qc,
      final InputInfo ii) throws QueryException {
    return (Item) invoke(fun, arg, true, qc, ii);
  }

  /**
   * Calls the given function with the given arguments, returning zero or more items.
   * This method takes care of tail calls.
   * @param fun function to call
   * @param arg arguments to the function
   * @param qc query context
   * @param ii input info
   * @return the resulting value
   * @throws QueryException query exception
   */
  public static Value value(final XQFunction fun, final Value[] arg, final QueryContext qc,
      final InputInfo ii) throws QueryException {
    return invoke(fun, arg, false, qc, ii);
  }
}

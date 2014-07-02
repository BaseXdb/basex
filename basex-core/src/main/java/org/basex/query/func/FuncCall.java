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
   * @param ctx query context
   * @return the function
   * @throws QueryException query exception
   */
  abstract XQFunction evalFunc(final QueryContext ctx) throws QueryException;

  /**
   * Evaluates and returns the arguments for this call.
   * @param ctx query context
   * @return the function
   * @throws QueryException query exception
   */
  abstract Value[] evalArgs(final QueryContext ctx) throws QueryException;

  @Override
  public final void markTailCalls(final QueryContext ctx) {
    if (ctx != null) ctx.compInfo(QueryText.OPTTCE, this);
    tailCall = true;
  }

  @Override
  public final Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    return tailCall ? (Item) invokeTail(evalFunc(ctx), evalArgs(ctx), true, ctx, info)
                    : (Item) invoke(evalFunc(ctx), evalArgs(ctx), true, ctx, info);
  }

  @Override
  public final Value value(final QueryContext ctx) throws QueryException {
    return tailCall ? invokeTail(evalFunc(ctx), evalArgs(ctx), false, ctx, info)
                    : invoke(evalFunc(ctx), evalArgs(ctx), false, ctx, info);
  }

  @Override
  public final Iter iter(final QueryContext ctx) throws QueryException {
    return value(ctx).iter();
  }

  /**
   * Calls the given function with the given arguments and takes care of tail-calls.
   * @param fun function to call
   * @param arg arguments for the call
   * @param ctx query context
   * @param itm flag for requesting a single item
   * @param ii input info
   * @return result of the function call
   * @throws QueryException query exception
   */
  private static Value invoke(final XQFunction fun, final Value[] arg, final boolean itm,
      final QueryContext ctx, final InputInfo ii) throws QueryException {

    XQFunction func = fun;
    Value[] args = arg;
    final int fp = ctx.stack.enterFrame(func.stackFrameSize());
    try {
      while(true) {
        final Value ret = itm ? func.invItem(ctx, ii, args) : func.invValue(ctx, ii, args);
        func = ctx.pollTailCall();
        if(func == null) return ret;
        ctx.stack.reuseFrame(func.stackFrameSize());
        args = ctx.pollTailArgs();
      }
    } catch(final QueryException ex) {
      ex.add(ii);
      throw ex;
   } finally {
      ctx.stack.exitFrame(fp);
    }
  }

  /**
   * Tail-calls the given function with the given arguments.
   * @param fun function to call
   * @param arg arguments for the call
   * @param ctx query context
   * @param itm flag for requesting a single item
   * @param ii input info
   * @return result of the function call
   * @throws QueryException query exception
   */
  private static Value invokeTail(final XQFunction fun, final Value[] arg, final boolean itm,
      final QueryContext ctx, final InputInfo ii) throws QueryException {

    final int calls = ctx.tailCalls, max = ctx.maxCalls;
    if(max >= 0 && calls >= max) {
      // there are at least `ctx.maxCalls` tail-calls on the stack, eliminate them
      ctx.registerTailCall(fun, arg);
      return itm ? null : Empty.SEQ;
    }

    ctx.tailCalls++;
    final int fp = ctx.stack.enterFrame(fun.stackFrameSize());
    try {
      return itm ? fun.invItem(ctx, ii, arg) : fun.invValue(ctx, ii, arg);
    } catch(final QueryException ex) {
      ex.add(ii);
      throw ex;
    } finally {
      ctx.tailCalls = calls;
      ctx.stack.exitFrame(fp);
    }
  }

  /**
   * Calls the given function with the given arguments, returning zero or one item.
   * This method takes care of tail calls.
   * @param fun function to call
   * @param arg arguments to the function
   * @param ctx query context
   * @param ii input info
   * @return the resulting item
   * @throws QueryException query exception
   */
  public static Item item(final XQFunction fun, final Value[] arg,
      final QueryContext ctx, final InputInfo ii) throws QueryException {
    return (Item) invoke(fun, arg, true, ctx, ii);
  }

  /**
   * Calls the given function with the given arguments, returning zero or more items.
   * This method takes care of tail calls.
   * @param fun function to call
   * @param arg arguments to the function
   * @param ctx query context
   * @param ii input info
   * @return the resulting value
   * @throws QueryException query exception
   */
  public static Value value(final XQFunction fun, final Value[] arg,
      final QueryContext ctx, final InputInfo ii) throws QueryException {
    return invoke(fun, arg, false, ctx, ii);
  }
}

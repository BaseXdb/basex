package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * An XQuery function call, either static or dynamic.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public abstract class FuncCall extends Arr {
  /** Tail-call flag. */
  boolean tailCall;

  /**
   * Constructor.
   * @param ii input info
   * @param e sub-expressions
   */
  FuncCall(final InputInfo ii, final Expr[] e) {
    super(ii, e);
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
  public final Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    return (Item) invoke(evalFunc(ctx), evalArgs(ctx), true, tailCall, ctx, info);
  }

  @Override
  public final Value value(final QueryContext ctx) throws QueryException {
    return invoke(evalFunc(ctx), evalArgs(ctx), false, tailCall, ctx, info);
  }

  @Override
  public final Iter iter(final QueryContext ctx) throws QueryException {
    return value(ctx).iter();
  }

  @Override
  public final void markTailCalls() {
    tailCall = true;
  }

  /**
   * Calls the given function with the given arguments and takes care of tail-calls.
   * @param fun function to call
   * @param arg arguments for the call
   * @param ctx query context
   * @param itm flag for requesting a single item
   * @param tc flag for a tail call
   * @param ii input info
   * @return result of the function call
   * @throws QueryException query exception
   */
  private static Value invoke(final XQFunction fun, final Value[] arg,
      final boolean itm, final boolean tc, final QueryContext ctx, final InputInfo ii)
          throws QueryException {

    final int calls = ctx.tailCalls, max = ctx.maxCalls;
    if(tc && max >= 0 && ++ctx.tailCalls >= max) {
      // there are at least `ctx.maxCalls` tail-calls on the stack, eliminate them
      throw new Continuation(fun, arg);
    }

    try {
      // tail-calls are evaluated immediately
      if(tc) return itm ? fun.invItem(ctx, ii, arg)
                        : fun.invValue(ctx, ii, arg);

      // non-tail-calls have to catch the continuations and resume from there
      XQFunction func = fun;
      Value[] args = arg;
      while(true) {
        try {
          return itm ? func.invItem(ctx, ii, args) : func.invValue(ctx, ii, args);
        } catch (final Continuation c) {
          func = c.func;
          args = c.args;
          ctx.tailCalls = calls;
        }
      }
    } catch(final QueryException ex) {
      ex.add(ii);
      throw ex;
    } finally {
      ctx.tailCalls = calls;
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
    return (Item) invoke(fun, arg, true, false, ctx, ii);
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
    return invoke(fun, arg, false, false, ctx, ii);
  }

  /**
   * A continuation that's thrown to free stack frames.
   * @author Leo Woerteler
   */
  private static final class Continuation extends RuntimeException {
    /** The function to call. */
    private final XQFunction func;
    /** The arguments to call the function with. */
    private final Value[] args;

    /**
     * Constructor.
     * @param fun function to call
     * @param arg arguments to the function
     */
    private Continuation(final XQFunction fun, final Value[] arg) {
      func = fun;
      args = arg;
    }

    @Override
    public synchronized Continuation fillInStackTrace() {
      // ignore this for efficiency reasons
      return this;
    }
  }
}

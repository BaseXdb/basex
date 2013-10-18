package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Interface for XQuery functions.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public interface XQFunction {

  /**
   * Number of arguments this function takes.
   * @return function arity
   */
  int arity();

  /**
   * Name of this function, {@code null} means anonymous function.
   * @return name or {@code null}
   */
  QNm fName();

  /**
   * Type of this function.
   * @return this function's type
   */
  FuncType funcType();

  /**
   * Internally invokes this function with the given arguments.
   * This method does not deal with tail calls, so it is unsafe to call.
   * Use {@link #invokeValue(QueryContext, InputInfo, Value...)} instead.
   * @param ctx query context
   * @param ii input info
   * @param args arguments
   * @return resulting iterator
   * @throws QueryException query exception
   */
  Value invValue(QueryContext ctx, InputInfo ii, Value... args) throws QueryException;

  /**
   * Internally invokes this function with the given arguments.
   * This method does not deal with tail calls, so it is unsafe to call.
   * Use {@link #invokeItem(QueryContext, InputInfo, Value...)} instead.
   * @param ctx query context
   * @param ii input info
   * @param args arguments
   * @return resulting item
   * @throws QueryException query exception
   */
  Item invItem(QueryContext ctx, InputInfo ii, Value... args) throws QueryException;

  /**
   * Invokes this function with the given arguments.
   * @param ctx query context
   * @param ii input info
   * @param args arguments
   * @return resulting iterator
   * @throws QueryException query exception
   */
  Value invokeValue(QueryContext ctx, InputInfo ii, Value... args) throws QueryException;

  /**
   * Invokes this function with the given arguments.
   * @param ctx query context
   * @param ii input info
   * @param args arguments
   * @return resulting item
   * @throws QueryException query exception
   */
  Item invokeItem(QueryContext ctx, InputInfo ii, Value... args) throws QueryException;

  /**
   * Tries to inline this function with the given argument expressions.
   * @param exprs argument expressions
   * @param ctx query context
   * @param scp variable scope
   * @param ii input info
   * @return the expression to inline if successful, {@code null} otherwise
   * @throws QueryException query exception
   */
  Expr inlineExpr(Expr[] exprs, QueryContext ctx, VarScope scp, InputInfo ii)
      throws QueryException;
}

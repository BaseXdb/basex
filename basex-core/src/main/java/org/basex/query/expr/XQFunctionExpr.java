package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Interface for possibly non-compiled XQuery functions.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public interface XQFunctionExpr {
  /**
   * Number of arguments this function takes.
   * @return function arity
   */
  int arity();

  /**
   * Name of this function, {@code null} means anonymous function.
   * @return name or {@code null}
   */
  QNm funcName();

  /**
   * Name of the argument at the given position.
   * @param pos position of the argument
   * @return name of the argument
   */
  QNm argName(final int pos);

  /**
   * Type of this function.
   * @return this function's type
   */
  FuncType funcType();

  /**
   * Annotations of this function.
   * @return this function's annotations
   */
  Ann annotations();

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

package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Interface for possibly non-compiled XQuery functions.
 *
 * @author BaseX Team 2005-21, BSD License
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
   * Name of the parameter at the given position.
   * @param pos position of the parameter
   * @return name of the parameter
   */
  QNm paramName(int pos);

  /**
   * Type of this function.
   * @return this function's type
   */
  FuncType funcType();

  /**
   * Annotations of this function.
   * @return this function's annotations
   */
  AnnList annotations();

  /**
   * Tries to inline this function with the given arguments.
   * @param exprs arguments
   * @param cc compilation context
   * @return the expression to inline if successful, {@code null} otherwise
   * @throws QueryException query exception
   */
  Expr inline(Expr[] exprs, CompileContext cc) throws QueryException;

  /**
   * Checks if this function returns vacuous results (see {@link Expr#vacuous()}).
   * @return result of check
   */
  boolean vacuousBody();
}

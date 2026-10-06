package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Interface for possibly non-compiled XQuery functions.
 *
 * @author BaseX Team, BSD License
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
   * @return name of the parameter, or {@code null}
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
   * Returns the name and arity of this function in the syntax of a named function reference.
   * @return label, or {@code null} if the function is anonymous
   */
  default byte[] funcLabel() {
    final QNm name = funcName();
    return name == null ? null : Token.concat(name.prefixId(), '#', arity());
  }

  /**
   * Returns the function name and its parameter names for error messages.
   * @return string, or {@code null} if the parameters are not named
   */
  default String paramString() {
    return null;
  }

  /**
   * Returns the name and the parameter names of the specified function for error messages.
   * @param func function
   * @param min number of required parameters
   * @return string
   */
  static String paramString(final XQFunctionExpr func, final int min) {
    final QNm name = func.funcName();
    if(name == null) return QueryText.FN;
    final TokenBuilder tb = new TokenBuilder();
    tb.add(name.prefixString()).add('(');
    final int arity = func.arity();
    for(int a = 0; a < arity; a++) {
      if(a > 0) tb.add(", ");
      tb.add(func.paramName(a).prefixString());
      if(a >= min) tb.add('?');
    }
    return tb.add(')').toString();
  }

  /**
   * Tries to inline this function with the given arguments.
   * @param exprs arguments
   * @param cc compilation context
   * @return expression to inline if successful, {@code null} otherwise
   * @throws QueryException query exception
   */
  Expr inline(Expr[] exprs, CompileContext cc) throws QueryException;

  /**
   * Checks if this function returns vacuous results (see {@link Expr#vacuous()}).
   * @return result of check
   */
  boolean vacuousBody();
}

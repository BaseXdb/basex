package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Interface for XQuery functions.
 *
 * @author BaseX Team 2005-12, BSD License
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
   * Invokes this function with the given arguments.
   * @param ctx query context
   * @param ii input info
   * @param args arguments
   * @return resulting iterator
   * @throws QueryException query exception
   */
  Value invValue(QueryContext ctx, InputInfo ii, Value... args) throws QueryException;

  /**
   * Invokes this function with the given arguments.
   * @param ctx query context
   * @param ii input info
   * @param args arguments
   * @return resulting item
   * @throws QueryException query exception
   */
  Item invItem(QueryContext ctx, InputInfo ii, Value... args) throws QueryException;
}

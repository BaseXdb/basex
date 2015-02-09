package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Interface for XQuery functions.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public interface XQFunction extends XQFunctionExpr {

  /**
   * Internally invokes this function with the given arguments.
   * This method does not deal with tail calls, so it is unsafe to call.
   * Use {@link #invokeValue(QueryContext, InputInfo, Value...)} instead.
   * @param qc query context
   * @param ii input info
   * @param args arguments
   * @return resulting iterator
   * @throws QueryException query exception
   */
  Value invValue(QueryContext qc, InputInfo ii, Value... args) throws QueryException;

  /**
   * Internally invokes this function with the given arguments.
   * This method does not deal with tail calls, so it is unsafe to call.
   * Use {@link #invokeItem(QueryContext, InputInfo, Value...)} instead.
   * @param qc query context
   * @param ii input info
   * @param args arguments
   * @return resulting item
   * @throws QueryException query exception
   */
  Item invItem(QueryContext qc, InputInfo ii, Value... args) throws QueryException;

  /**
   * Invokes this function with the given arguments.
   * @param qc query context
   * @param ii input info
   * @param args arguments
   * @return resulting iterator
   * @throws QueryException query exception
   */
  Value invokeValue(QueryContext qc, InputInfo ii, Value... args) throws QueryException;

  /**
   * Invokes this function with the given arguments.
   * @param qc query context
   * @param ii input info
   * @param args arguments
   * @return resulting item
   * @throws QueryException query exception
   */
  Item invokeItem(QueryContext qc, InputInfo ii, Value... args) throws QueryException;

  /**
   * Size of this function's stack frame.
   * @return stack frame size
   */
  int stackFrameSize();
}

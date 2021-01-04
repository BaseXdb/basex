package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Interface for XQuery functions.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Leo Woerteler
 */
public interface XQFunction extends XQFunctionExpr {
  /**
   * Calls the function with the given arguments and takes care of tail calls.
   * @param args arguments for the call
   * @param qc query context
   * @param item flag for requesting a single item
   * @param ii input info
   * @return result of the function call
   * @throws QueryException query exception
   */
  default Value invoke(final Value[] args, final boolean item, final QueryContext qc,
      final InputInfo ii) throws QueryException {

    XQFunction fn = this;
    Value[] values = args;
    final int fp = qc.stack.enterFrame(fn.stackFrameSize());
    try {
      while(true) {
        qc.checkStop();
        final Value value = fn.invoke(qc, ii, item, values);
        fn = qc.pollTailCall();
        if(fn == null) return value;
        qc.stack.reuseFrame(fn.stackFrameSize());
        values = qc.pollTailArgs();
      }
    } catch(final QueryException ex) {
      throw ex.add(ii);
   } finally {
      qc.stack.exitFrame(fp);
    }
  }

  /**
   * Internally invokes this function with the given arguments.
   * This method does not deal with tail calls, so it is unsafe to call.
   * Use {@link FItem#invokeValue(QueryContext, InputInfo, Value...)} instead.
   * @param qc query context
   * @param ii input info
   * @param item flag for requesting a single item
   * @param args arguments
   * @return resulting value
   * @throws QueryException query exception
   */
  Value invoke(QueryContext qc, InputInfo ii, boolean item, Value... args) throws QueryException;

  /**
   * Size of this function's stack frame.
   * @return stack frame size
   */
  int stackFrameSize();
}

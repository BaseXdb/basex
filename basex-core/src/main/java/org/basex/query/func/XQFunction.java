package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Interface for XQuery functions.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public interface XQFunction extends XQFunctionExpr {
  /**
   * Calls the function with the given arguments and takes care of tail calls.
   * Must not be overwritten.
   * @param qc query context
   * @param info input info (can be {@code null})
   * @param args arguments for the call (possibly more than required)
   * @return result of the function call
   * @throws QueryException query exception
   */
  default Value invoke(final QueryContext qc, final InputInfo info, final Value... args)
      throws QueryException {

    final int fp = qc.stack.enterFrame(stackFrameSize());
    try {
      XQFunction fn = this;
      Value[] values = args;
      while(true) {
        qc.checkStop();
        final Value value = fn.invokeInternal(qc, info, values);
        if(qc.tcFunc == null) return value;
        // call registered tail call function
        fn = qc.tcFunc;
        qc.tcFunc = null;
        values = qc.tcArgs;
        qc.tcArgs = null;
        qc.stack.reuseFrame(fn.stackFrameSize());
      }
    } catch(final QueryException ex) {
      throw ex.add(info);
    } finally {
      qc.stack.exitFrame(fp);
    }
  }

  /**
   * Tail-calls the given function with the given arguments.
   * Must not be overwritten.
   * @param qc query context
   * @param info input info (can be {@code null})
   * @param args arguments for the call
   * @return result of the function call
   * @throws QueryException query exception
   */
  default Value invokeTail(final QueryContext qc, final InputInfo info, final Value[] args)
      throws QueryException {

    qc.checkStop();
    final int size = stackFrameSize();
    if(qc.tco && qc.stack.tco(size)) {
      // stack is full: register tail call function
      qc.tcFunc = this;
      qc.tcArgs = args;
      return Empty.VALUE;
    }

    final int fp = qc.stack.enterFrame(size);
    try {
      return invokeInternal(qc, info, args);
    } catch(final QueryException ex) {
      throw ex.add(info);
    } finally {
      qc.stack.exitFrame(fp);
    }
  }

  /**
   * Internally invokes this function with the given arguments.
   * @param qc query context
   * @param info input info (can be {@code null})
   * @param args arguments
   * @return resulting value
   * @throws QueryException query exception
   */
  Value invokeInternal(QueryContext qc, InputInfo info, Value[] args) throws QueryException;

  /**
   * Size of this function's stack frame.
   * @return stack frame size
   */
  int stackFrameSize();
}

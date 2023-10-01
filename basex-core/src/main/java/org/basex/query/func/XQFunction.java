package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Interface for XQuery functions.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Leo Woerteler
 */
public interface XQFunction extends XQFunctionExpr {
  /**
   * Calls the function with the given arguments and takes care of tail calls.
   * Must not be overwritten.
   * @param qc query context
   * @param info input info (can be {@code null})
   * @param args arguments for the call
   * @return result of the function call
   * @throws QueryException query exception
   */
  default Value invoke(final QueryContext qc, final InputInfo info, final Value... args)
      throws QueryException {

    XQFunction fn = this;
    Value[] values = args;
    final int fp = qc.stack.enterFrame(fn.stackFrameSize());
    try {
      while(true) {
        qc.checkStop();
        final Value value = fn.invokeInternal(qc, info, values);
        fn = qc.pollTailCall();
        if(fn == null) return value;
        qc.stack.reuseFrame(fn.stackFrameSize());
        values = qc.pollTailArgs();
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
  default Value invokeTail(final QueryContext qc, final InputInfo info, final Value... args)
      throws QueryException {

    qc.checkStop();
    final int calls = qc.tailCalls, max = qc.maxCalls;
    if(max >= 0 && calls >= max) {
      // too many tail calls on the stack, eliminate them
      qc.registerTailCall(this, args);
      return Empty.VALUE;
    }

    qc.tailCalls++;
    final int fp = qc.stack.enterFrame(stackFrameSize());
    try {
      return invokeInternal(qc, info, args);
    } catch(final QueryException ex) {
      throw ex.add(info);
    } finally {
      qc.tailCalls = calls;
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

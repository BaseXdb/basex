package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public class FnEmpty extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(empty(qc));
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Bln empty = opt();
    return empty == null ? this : empty;
  }

  /**
   * Evaluates the function.
   * @param qc query context
   * @return boolean result
   * @throws QueryException query exception
   */
  final boolean empty(final QueryContext qc) throws QueryException {
    final Expr expr = exprs[0];
    return expr.seqType().zeroOrOne() ?
      expr.item(qc, info) == Empty.VALUE :
      expr.iter(qc).next() == null;
  }

  /**
   * Optimizes an existence check.
   * @return boolean result, or {@code null} if no optimization is possible
   */
  final Bln opt() {
    // ignore non-deterministic expressions (e.g.: empty(error()))
    final Expr expr = exprs[0];
    if(!expr.has(Flag.NDT)) {
      final long size = expr.size();
      if(size != -1) return Bln.get(size == 0);
      if(expr.seqType().oneOrMore()) return Bln.FALSE;
    }
    return null;
  }
}

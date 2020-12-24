package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
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
  protected Expr opt(final CompileContext cc) throws QueryException {
    return opt(true, cc);
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
   * @param empty empty flag
   * @param cc compilation context
   * @return boolean result or original expression
   * @throws QueryException query exception
   */
  final Expr opt(final boolean empty, final CompileContext cc) throws QueryException {
    final Expr expr = exprs[0];
    final SeqType st = expr.seqType();

    // ignore non-deterministic expressions (e.g.: empty(error()))
    if(!expr.has(Flag.NDT)) {
      if(st.zero()) return Bln.get(empty);
      if(st.oneOrMore()) return Bln.get(!empty);
    }

    // rewrite list to union expression
    if(expr instanceof List && expr.seqType().type instanceof NodeType) {
      exprs[0] = new Union(info, expr.args()).optimize(cc);
    }

    // simplify argument
    final Expr arg = FnCount.simplify(expr, cc);
    return arg != null ? cc.function(empty ? EMPTY : EXISTS, info, arg) : this;
  }

  @Override
  public Expr mergeEbv(final Expr expr, final boolean or, final CompileContext cc)
      throws QueryException {

    if(!or && Function.EMPTY.is(expr)) {
      final Expr args = List.get(cc, info, exprs[0], expr.arg(0));
      return cc.function(Function.EMPTY, info, args);
    }
    return null;
  }
}

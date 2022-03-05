package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.CmpG.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
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
    Expr expr = exprs[0];
    final SeqType st = expr.seqType();

    // ignore non-deterministic expressions (e.g.: empty(error()))
    if(!expr.has(Flag.NDT)) {
      if(st.zero()) return Bln.get(empty);
      if(st.oneOrMore()) return Bln.get(!empty);
    }

    // rewrite util:replicate
    if(_UTIL_REPLICATE.is(expr)) expr = expr.arg(0);

    // rewrite list to union expression:  exists((a, b))  ->  exists(a | b)
    if(expr instanceof List && expr.seqType().type instanceof NodeType) {
      expr = new Union(info, expr.args()).optimize(cc);
    }
    // rewrite filter:  exists($a[text() = 'Ukraine'])  ->  $a/text() = 'Ukraine'
    if(expr instanceof Filter) {
      final Filter filter = (Filter) expr;
      expr = filter.flattenEbv(filter.root, false, cc);
      if(expr != filter) return cc.function(empty ? NOT : BOOLEAN, info, expr);
    }
    // simplify argument:  exists(sort($nodes))  ->  exists($nodex)
    expr = simplify(expr, cc);

    // rewrite index-of:  exists(index-of($texts, 'Ukraine')) ->  $texts = 'Ukraine'
    if(INDEX_OF.is(expr)) {
      final Expr[] args = expr.args();
      if(args.length == 2 && args[1].seqType().one() &&
          CmpG.compatible(args[0].seqType(), args[1].seqType(), true)) {
        expr = new CmpG(args[0], args[1], OpG.EQ, null, sc, info).optimize(cc);
        return cc.function(empty ? NOT : BOOLEAN, info, expr);
      }
    }

    return expr != exprs[0] ? cc.function(empty ? EMPTY : EXISTS, info, expr) : this;
  }

  /**
   * Simplify arguments of existence and count operations.
   * @param expr expression to simplify
   * @param cc compilation context
   * @return simplified or supplied expression
   * @throws QueryException query exception
   */
  public static Expr simplify(final Expr expr, final CompileContext cc) throws QueryException {
    // simplify filter expression
    if(expr instanceof Filter) {
      return ((Filter) expr).simplifyCount(cc);
    }
    // rewrite count(reverse(...)) to count(...)
    if(REVERSE.is(expr) || SORT.is(expr)) {
      return expr.arg(0);
    }
    // remove order by clauses from FLWOR expression
    if(expr instanceof GFLWOR) {
      final Expr flwor = ((GFLWOR) expr).removeOrderBy(cc);
      if(flwor != null) return flwor;
    }
    return expr;
  }

  @Override
  public Expr mergeEbv(final Expr expr, final boolean or, final CompileContext cc)
      throws QueryException {

    if(!or && EMPTY.is(expr)) {
      final Expr args = List.get(cc, info, exprs[0], expr.arg(0));
      return cc.function(EMPTY, info, args);
    }
    if(_UTIL_COUNT_WITHIN.is(expr)) {
      return expr.mergeEbv(this, or, cc);
    }
    return null;
  }
}

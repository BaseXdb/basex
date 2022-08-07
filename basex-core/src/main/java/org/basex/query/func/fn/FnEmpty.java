package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.expr.CmpG.*;
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
  protected final void simplifyArgs(final CompileContext cc) throws QueryException {
    exprs[0] = exprs[0].simplifyFor(Simplify.COUNT, cc);
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final boolean exists = this instanceof FnExists;
    Expr expr = exprs[0];
    final SeqType st = expr.seqType();

    // ignore non-deterministic expressions (e.g.: empty(error()))
    if(!expr.has(Flag.NDT)) {
      if(st.zero()) return Bln.get(!exists);
      if(st.oneOrMore()) return Bln.get(exists);
    }

    // static integer will always be greater than 1
    if(REPLICATE.is(expr) && expr.arg(1) instanceof Int) {
      expr = expr.arg(0);
    }
    // rewrite list to union expression:  exists((a, b))  ->  exists(a | b)
    if(expr instanceof List && expr.seqType().type instanceof NodeType) {
      expr = new Union(info, expr.args()).optimize(cc);
    }
    if(expr != exprs[0]) return cc.function(exists ? EXISTS : EMPTY, info, expr);

    // replace optimized expression by boolean function
    if(expr instanceof Filter) {
      // rewrite filter:  exists($a[text() = 'Ukraine'])  ->  $a/text() = 'Ukraine'
      final Filter filter = (Filter) expr;
      expr = filter.flattenEbv(filter.root, false, cc);
    } else if(INDEX_OF.is(expr)) {
      // rewrite index-of:  exists(index-of($texts, 'Ukraine'))  ->  $texts = 'Ukraine'
      final Expr[] args = expr.args();
      if(args.length == 2 && args[1].seqType().one() &&
          CmpG.compatible(args[0].seqType(), args[1].seqType(), true)) {
        expr = new CmpG(args[0], args[1], OpG.EQ, null, sc, info).optimize(cc);
      }
    } else if(STRING_TO_CODEPOINTS.is(expr) || CHARACTERS.is(expr)) {
      // exists(string-to-codepoints(...))  ->  boolean(string(...))
      expr = cc.function(STRING, info, expr.args());
    }
    if(expr != exprs[0]) return cc.function(exists ? BOOLEAN : NOT, info, expr);

    // exists(map:keys(...))  ->  map:size(...) > 0
    if(_MAP_KEYS.is(expr)) {
      expr = cc.function(_MAP_SIZE, info, expr.args());
      return new CmpG(expr, Int.ZERO, exists ? OpG.NE : OpG.EQ, null, sc, info).optimize(cc);
    }

    final Expr embedded = embed(cc, true);
    if(embedded != null) return embedded;

    return this;
  }

  @Override
  public final Expr mergeEbv(final Expr expr, final boolean or, final CompileContext cc)
      throws QueryException {

    final Function func = this instanceof FnExists ? EXISTS : EMPTY;
    if(!or && func.is(expr)) {
      final Expr args = List.get(cc, info, exprs[0], expr.arg(0));
      return cc.function(func, info, args);
    }
    if(_UTIL_COUNT_WITHIN.is(expr)) {
      return expr.mergeEbv(this, or, cc);
    }
    return null;
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
}

package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class FnBoolean extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(exprs[0].ebv(qc, info).bool(info));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // e.g.: boolean(exists(<a/>)) -> boolean(<a/>)
    final Expr expr = cc.simplifyEbv(exprs[0]);

    // boolean($node/text()) -> exists($node/text())
    final SeqType st = expr.seqType();
    if(st.type instanceof NodeType) return cc.function(Function.EXISTS, info, expr);

    // simplify, e.g.: boolean(true())) -> true()
    if(st.eq(SeqType.BLN_O)) return expr;

    exprs[0] = expr;
    return this;
  }

  @Override
  public Expr simplify(final CompileContext cc, final Simplify simplify) {
    // if A is not numeric: expr[boolean(A)] -> expr[A]
    if(simplify == Simplify.EBV) {
      final Expr expr = exprs[0];
      if(!expr.seqType().mayBeNumber()) return cc.simplify(this, expr);
    }
    return this;
  }

  /**
   * Returns a boolean equivalent for the specified expression.
   * If the specified expression yields a boolean value anyway, it will be
   * returned as is. Otherwise, it will be wrapped into a boolean function.
   * @param expr expression to be rewritten
   * @param ii input info
   * @param sc static context
   * @return expression
   */
  public static Expr get(final Expr expr, final InputInfo ii, final StaticContext sc) {
    return expr.seqType().eq(SeqType.BLN_O) ? expr : Function.BOOLEAN.get(sc, ii, expr);
  }
}

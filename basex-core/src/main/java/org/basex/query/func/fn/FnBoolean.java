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
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class FnBoolean extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(exprs[0].ebv(qc, info).bool(info));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // boolean(exists(<a/>))  ->  boolean(<a/>)
    final Expr expr = exprs[0].simplifyFor(Simplify.EBV, cc);

    // boolean(true()))  ->  true()
    final SeqType st = expr.seqType();
    if(st.eq(SeqType.BLN_O)) return expr;

    // boolean($node/text())  ->  exists($node/text())
    if(st.type instanceof NodeType) return cc.function(Function.EXISTS, info, expr);

    exprs[0] = expr;
    return this;
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) {
    // if A is not numeric: expr[boolean(A)]  ->  expr[A]
    if(mode == Simplify.EBV) {
      final Expr expr = exprs[0];
      if(!expr.seqType().mayBeNumber()) return cc.simplify(this, expr);
    }
    return this;
  }
}

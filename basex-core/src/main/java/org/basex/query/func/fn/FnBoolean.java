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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnBoolean extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(exprs[0].ebv(qc, info).bool(info));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // boolean(true()))  ->  true()
    final Expr expr = exprs[0];
    final SeqType st = expr.seqType();
    if(st.eq(SeqType.BOOLEAN_O)) return expr;

    // boolean($node/text())  ->  exists($node/text())
    if(st.type instanceof NodeType) return cc.function(Function.EXISTS, info, expr);

    return this;
  }

  @Override
  protected void simplifyArgs(final CompileContext cc) throws QueryException {
    // boolean(exists(<a/>))  ->  boolean(<a/>)
    exprs[0] = exprs[0].simplifyFor(Simplify.EBV, cc);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) {
    // if(boolean(number))  ->  if(number)
    // E[boolean(nodes)]  ->  E[nodes]
    final Expr expr = exprs[0];
    return mode == Simplify.EBV || mode == Simplify.PREDICATE && !expr.seqType().mayBeNumber() ?
      cc.simplify(this, expr) : this;
  }
}
